package org.folio.qm.service.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_999_FIELD;
import static org.folio.qm.domain.entity.JobProfileAction.CREATE;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.client.LinksSuggestionsClient;
import org.folio.qm.client.UsersClient;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.OptimisticLockingException;
import org.folio.qm.exception.UnexpectedException;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.mapper.LinksSuggestionsMapper;
import org.folio.qm.mapper.UserMapper;
import org.folio.qm.service.ChangeManagerService;
import org.folio.qm.service.DataImportJobService;
import org.folio.qm.service.FieldProtectionSetterService;
import org.folio.qm.service.LinksService;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.StatusService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.validation.SkippedValidationError;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@RequiredArgsConstructor
@Log4j2
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private static final String RECORD_NOT_FOUND_MESSAGE = "Record with id [%s] was not found";
  private static final Predicate<FieldItem> FIELD_001_PREDICATE =
    qmFields -> qmFields.getTag().equals(TAG_001_CONTROL_FIELD);
  private static final Predicate<FieldItem> FIELD_999_PREDICATE = qmFields -> qmFields.getTag().equals(TAG_999_FIELD);
  private static final Predicate<FieldItem> FIELD_EMPTY_PREDICATE = qmFields -> {
    final var content = qmFields.getContent();
    return content instanceof String stringContent && StringUtils.isEmpty(stringContent);
  };

  private final ChangeManagerService changeManagerService;
  private final DataImportJobService dataImportJobService;
  private final DefaultValuesPopulationService defaultValuesPopulationService;
  private final FieldProtectionSetterService protectionSetterService;
  private final ConversionService conversionService;
  private final ValidationService validationService;
  private final StatusService statusService;
  private final LinksService linksService;

  private final UsersClient usersClient;
  private final LinksSuggestionsClient linksSuggestionsClient;

  private final LinksSuggestionsMapper linksSuggestionsMapper;
  private final CreationStatusMapper statusMapper;
  private final UserMapper userMapper;

  private final FolioExecutionContext folioExecutionContext;

  @Override
  public QuickMarcView findByExternalId(UUID externalId) {
    log.debug("findByExternalId:: trying to find quickMarc by externalId: {}", externalId);
    var sourceRecord = changeManagerService.getSourceRecordByExternalId(externalId.toString());
    var quickMarc = conversionService.convert(sourceRecord, QuickMarcView.class);
    protectionSetterService.applyFieldProtection(quickMarc);
    linksService.setRecordLinks(quickMarc);
    setUserInfo(quickMarc, sourceRecord);
    log.info("findByExternalId:: quickMarc loaded by externalId: {}", externalId);
    return quickMarc;
  }

  @Override
  public void updateById(UUID parsedRecordId, QuickMarcEdit quickMarc,
                         DeferredResult<ResponseEntity<Void>> updateResult) {
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", parsedRecordId);
    defaultValuesPopulationService.populate(quickMarc);
    validateOnUpdate(parsedRecordId, quickMarc);
    var parsedRecordDto = conversionService.convert(quickMarc, ParsedRecordDto.class);
    updateResult.onCompletion(updateLinksTask(folioExecutionContext, quickMarc, updateResult));
    changeManagerService.putParsedRecordByInstanceId(quickMarc.getParsedRecordDtoId(), parsedRecordDto);
    log.info("updateById:: quickMarc updated by parsedRecordId: {}", parsedRecordId);
  }

  @Override
  public CreationStatus getCreationStatusByQmRecordId(UUID qmRecordId) {
    log.debug("getCreationStatusByQmRecordId:: trying to get creationStatus by qmRecordId: {}", qmRecordId);
    return statusService.findById(qmRecordId)
      .map(status -> {
        log.info("getCreationStatusByQmRecordId:: creationStatus: {} loaded by qmRecordId: {}",
          status.getStatus(), qmRecordId);
        return statusMapper.fromEntity(status);
      })
      .orElseThrow(() -> new NotFoundException(String.format(RECORD_NOT_FOUND_MESSAGE, qmRecordId)));
  }

  @Override
  public CreationStatus createNewRecord(QuickMarcCreate quickMarc) {
    log.debug("createNewRecord:: trying to create a new quickMarc");
    defaultValuesPopulationService.populate(quickMarc);
    validateOnCreate(quickMarc);
    var recordDto = conversionService.convert(prepareRecord(quickMarc), ParsedRecordDto.class);
    var status = runImportAndGetStatus(recordDto, CREATE);
    log.info("createNewRecord:: new quickMarc created with qmRecordId: {}", status.getQmRecordId());
    return status;
  }

  @Override
  public QuickMarcView suggestLinks(QuickMarcView quickMarcView, AuthoritySearchParameter authoritySearchParameter,
                                    Boolean ignoreAutoLinkingEnabled) {
    log.debug("suggestLinks:: trying to suggest links");
    var srsRecords = linksSuggestionsMapper.map(List.of(quickMarcView));
    var srsRecordsWithSuggestions = linksSuggestionsClient.postLinksSuggestions(srsRecords, authoritySearchParameter,
      ignoreAutoLinkingEnabled);
    var quickMarcRecordsWithSuggestions = linksSuggestionsMapper.map(srsRecordsWithSuggestions);
    if (isNotEmpty(quickMarcRecordsWithSuggestions)) {
      log.info("suggestLinks:: links was suggested");
      return quickMarcRecordsWithSuggestions.getFirst();
    }
    return quickMarcView;
  }

  private QuickMarcCreate prepareRecord(QuickMarcCreate quickMarc) {
    var fieldItemPredicate = FIELD_EMPTY_PREDICATE.or(FIELD_999_PREDICATE);
    if (quickMarc.getMarcFormat() != MarcFormat.AUTHORITY) {
      fieldItemPredicate = fieldItemPredicate.or(FIELD_001_PREDICATE);
    }
    quickMarc.getFields().removeIf(fieldItemPredicate);
    return quickMarc;
  }

  private CreationStatus runImportAndGetStatus(ParsedRecordDto recordDto, JobProfileAction action) {
    var jobId = dataImportJobService.executeDataImportJob(recordDto, action);
    return statusService.findByJobExecutionId(jobId)
      .map(statusMapper::fromEntity)
      .orElseThrow(() -> new UnexpectedException(String.format(RECORD_NOT_FOUND_MESSAGE, jobId)));
  }

  private void validateOnCreate(QuickMarcCreate quickMarc) {
    var skippedValidationError = new SkippedValidationError(TAG_001_CONTROL_FIELD, MarcRuleCode.MISSING_FIELD);
    validationService.validateMarcRecord(quickMarc, List.of(skippedValidationError));
    validateMarcRecord(quickMarc);
  }

  private void validateOnUpdate(UUID parsedRecordId, QuickMarcEdit quickMarc) {
    var requestVersion = quickMarc.getSourceVersion();
    var storedVersion =
      changeManagerService.getSourceRecordByExternalId(quickMarc.getExternalId().toString()).getGeneration();
    if (requestVersion != null && !requestVersion.equals(storedVersion)) {
      throw new OptimisticLockingException(parsedRecordId, storedVersion, requestVersion);
    }
    validationService.validateMarcRecord(quickMarc, Collections.emptyList());
    validationService.validateIdsMatch(quickMarc, parsedRecordId);
    validateMarcRecord(quickMarc);
  }

  private void validateMarcRecord(BaseMarcRecord marcRecord) {
    var validationResult = validationService.validate(marcRecord);
    if (!validationResult.isValid()) {
      throw new FieldsValidationException(validationResult);
    }
  }

  private void setUserInfo(QuickMarcView quickMarc, SourceRecord sourceRecord) {
    if (sourceRecord.getMetadata() != null && sourceRecord.getMetadata().getUpdatedByUserId() != null) {
      usersClient.fetchUserById(sourceRecord.getMetadata().getUpdatedByUserId())
        .ifPresent(userDto -> {
          var userInfo = userMapper.fromDto(userDto);
          Objects.requireNonNull(quickMarc).getUpdateInfo().setUpdatedBy(userInfo);
        });
    }
  }

  private Runnable updateLinksTask(FolioExecutionContext executionContext,
                                   QuickMarcEdit quickMarc,
                                   DeferredResult<ResponseEntity<Void>> updateResult) {
    var newContext = new DefaultFolioExecutionContext(executionContext.getFolioModuleMetadata(),
      executionContext.getAllHeaders());
    return () -> {
      @SuppressWarnings("unchecked")
      var result = (ResponseEntity<Void>) updateResult.getResult();
      if (result == null || result.getStatusCode().isError()) {
        return;
      }
      runInFolioContext(newContext, () -> linksService.updateRecordLinks(quickMarc));
    };
  }
}
