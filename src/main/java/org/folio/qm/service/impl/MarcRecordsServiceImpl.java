package org.folio.qm.service.impl;

import static org.folio.qm.domain.entity.JobProfileAction.CREATE;
import static org.folio.qm.domain.entity.JobProfileAction.DELETE;
import static org.folio.qm.util.MarcUtils.updateRecordTimestamp;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.client.UsersClient;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.UnexpectedException;
import org.folio.qm.mapper.CreationStatusMapper;
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
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@RequiredArgsConstructor
@Log4j2
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private static final String RECORD_NOT_FOUND_MESSAGE = "Record with id [%s] was not found";

  private final ChangeManagerService changeManagerService;
  private final DataImportJobService dataImportJobService;
  private final UsersClient usersClient;
  private final ValidationService validationService;
  private final StatusService statusService;
  private final FieldProtectionSetterService protectionSetterService;
  private final DefaultValuesPopulationService defaultValuesPopulationService;
  private final LinksService linksService;

  private final CreationStatusMapper statusMapper;
  private final UserMapper userMapper;
  private final Converter<QuickMarc, ParsedRecordDto> qmConverter;
  private final Converter<ParsedRecordDto, QuickMarc> dtoConverter;

  private final FolioExecutionContext folioExecutionContext;

  @Override
  public QuickMarc findByExternalId(UUID externalId) {
    log.debug("findByExternalId:: trying to find quickMarc by externalId: {}", externalId);
    var parsedRecordDto = changeManagerService.getParsedRecordByExternalId(externalId.toString());
    var quickMarc = dtoConverter.convert(parsedRecordDto);

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
    var status  = runImportAndGetStatus(recordDto, DELETE);
    log.info("deleteByExternalId:: quickMarc deleted with status: {}", status.getStatus());
    return status;
  }

  @Override
  public void updateById(UUID parsedRecordId, QuickMarc quickMarc, DeferredResult<ResponseEntity<Void>> updateResult) {
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", parsedRecordId);
    validationService.validateIdsMatch(quickMarc, parsedRecordId);
    populateWithDefaultValuesAndValidateMarcRecord(quickMarc);
    var parsedRecordDto = qmConverter.convert(updateRecordTimestamp(quickMarc));
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
  public CreationStatus createNewRecord(QuickMarc quickMarc) {
    log.debug("createNewRecord:: trying to create a new quickMarc");
    populateWithDefaultValuesAndValidateMarcRecord(quickMarc);
    var recordDto = qmConverter.convert(prepareRecord(quickMarc));
    var status  = runImportAndGetStatus(recordDto, CREATE);
    log.info("createNewRecord:: new quickMarc created with qmRecordId: {}", status.getQmRecordId());
    return status;
  }

  private QuickMarc prepareRecord(QuickMarc quickMarc) {
    clearFields(quickMarc);
    updateRecordTimestamp(quickMarc);
    return quickMarc;
  }

  private CreationStatus runImportAndGetStatus(ParsedRecordDto recordDto, JobProfileAction action) {
    var jobId = dataImportJobService.executeDataImportJob(recordDto, action);
    return statusService.findByJobExecutionId(jobId)
      .map(statusMapper::fromEntity)
      .orElseThrow(() -> new UnexpectedException(String.format(RECORD_NOT_FOUND_MESSAGE, jobId)));
  }

  private void clearFields(QuickMarc quickMarc) {
    final Predicate<FieldItem> field999Predicate = qmFields -> qmFields.getTag().equals("999");
    final Predicate<FieldItem> emptyContentPredicate = qmFields -> {
      final var content = qmFields.getContent();
      return content instanceof String && StringUtils.isEmpty((String) content);
    };
    quickMarc.getFields().removeIf(field999Predicate.or(emptyContentPredicate));
    quickMarc.setParsedRecordId(null);
    quickMarc.setParsedRecordDtoId(null);
    quickMarc.setExternalId(null);
    quickMarc.setExternalHrid(null);
  }

  private void populateWithDefaultValuesAndValidateMarcRecord(QuickMarc quickMarc) {
    defaultValuesPopulationService.populate(quickMarc);
    var validationResult = validationService.validate(quickMarc);
    if (!validationResult.isValid()) {
      throw new FieldsValidationException(validationResult);
    }
  }

  private void setUserInfo(QuickMarc quickMarc, ParsedRecordDto parsedRecordDto) {
    if (parsedRecordDto.getMetadata() != null && parsedRecordDto.getMetadata().getUpdatedByUserId() != null) {
      usersClient.fetchUserById(parsedRecordDto.getMetadata().getUpdatedByUserId())
        .ifPresent(userDto -> {
          var userInfo = userMapper.fromDto(userDto);
          Objects.requireNonNull(quickMarc).getUpdateInfo().setUpdatedBy(userInfo);
        });
    }
  }

  private Runnable updateLinksTask(FolioExecutionContext executionContext,
                                   QuickMarc quickMarc,
                                   DeferredResult<ResponseEntity<Void>> updateResult) {
    var newContext = new DefaultFolioExecutionContext(executionContext.getFolioModuleMetadata(),
      executionContext.getAllHeaders());
    return () -> {
      var result = (ResponseEntity<Void>) updateResult.getResult();
      if (result == null || result.getStatusCode().isError()) {
        return;
      }
      runInFolioContext(newContext, () -> linksService.updateRecordLinks(quickMarc));
    };
  }
}
