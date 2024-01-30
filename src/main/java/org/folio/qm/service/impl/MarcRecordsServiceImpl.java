package org.folio.qm.service.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_999_FIELD;
import static org.folio.qm.domain.entity.JobProfileAction.CREATE;
import static org.folio.qm.domain.entity.JobProfileAction.DELETE;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.client.LinksSuggestionsClient;
import org.folio.qm.client.UsersClient;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.exception.FieldsValidationException;
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
    var parsedRecordDto = changeManagerService.getParsedRecordByExternalId(externalId.toString());
    var quickMarc = conversionService.convert(parsedRecordDto, QuickMarcView.class);

    protectionSetterService.applyFieldProtection(quickMarc);
    linksService.setRecordLinks(quickMarc);
    setUserInfo(quickMarc, parsedRecordDto);
    log.info("findByExternalId:: quickMarc loaded by externalId: {}", externalId);
    return quickMarc;
  }

  @Override
  public CreationStatus deleteByExternalId(UUID externalId) {
    log.debug("deleteByExternalId:: trying to delete quickMarc by externalId: {}", externalId);
    var recordDto = changeManagerService.getParsedRecordByExternalId(externalId.toString());
    var status = runImportAndGetStatus(recordDto, DELETE);
    log.info("deleteByExternalId:: quickMarc deleted with status: {}", status.getStatus());
    return status;
  }

  @Override
  public void updateById(UUID parsedRecordId, QuickMarcEdit quickMarc,
                         DeferredResult<ResponseEntity<Void>> updateResult) {
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", parsedRecordId);
    validationService.validateIdsMatch(quickMarc, parsedRecordId);
    populateWithDefaultValuesAndValidateMarcRecord(quickMarc);
    var parsedRecordDto = conversionService.convert(quickMarc, ParsedRecordDto.class);
    System.out.println("tsaghik parsedRecordDto: " + parsedRecordDto);
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
    populateWithDefaultValuesAndValidateMarcRecord(quickMarc);
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
      return quickMarcRecordsWithSuggestions.get(0);
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

  private void populateWithDefaultValuesAndValidateMarcRecord(BaseMarcRecord quickMarc) {
    defaultValuesPopulationService.populate(quickMarc);
    var validationResult = validationService.validate(quickMarc);
    if (!validationResult.isValid()) {
      throw new FieldsValidationException(validationResult);
    }
  }

  private void setUserInfo(QuickMarcView quickMarc, ParsedRecordDto parsedRecordDto) {
    if (parsedRecordDto.getMetadata() != null && parsedRecordDto.getMetadata().getUpdatedByUserId() != null) {
      usersClient.fetchUserById(parsedRecordDto.getMetadata().getUpdatedByUserId())
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
