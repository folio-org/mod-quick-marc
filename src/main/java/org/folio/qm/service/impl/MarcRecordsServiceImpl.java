package org.folio.qm.service.impl;

import static org.folio.qm.converter.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.ChangeManagerPayloadUtils.getDefaultJobProfile;
import static org.folio.qm.util.ChangeManagerPayloadUtils.getDefaultJodExecutionDto;
import static org.folio.qm.util.ChangeManagerPayloadUtils.getRawRecordsBody;
import static org.folio.qm.util.MarcUtils.encodeToMarcDateTime;
import static org.folio.qm.util.MarcUtils.getFieldByTag;
import static org.folio.qm.util.StatusUtils.getStatusInProgress;
import static org.folio.qm.util.StatusUtils.getStatusNew;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import org.mapstruct.ap.internal.util.Strings;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.QuickMarcFields;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.util.JobExecutionProfileProperties;
import org.folio.qm.util.JsonUtils;
import org.folio.rest.jaxrs.model.InitialRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private static final String RECORD_NOT_FOUND_MESSAGE = "Record with id [%s] was not found";

  private final SRMChangeManagerClient srmClient;
  private final ValidationService validationService;
  private final CreationStatusService statusService;

  private final Converter<ParsedRecordDto, QuickMarc> parsedRecordToQuickMarcConverter;
  private final Converter<QuickMarc, ParsedRecordDto> quickMarcToParsedRecordConverter;
  private final CreationStatusMapper statusMapper;
  private final FolioExecutionContext folioExecutionContext;
  private final JobExecutionProfileProperties jobExecutionProfileProperties;

  @Override
  public QuickMarc findByInstanceId(UUID instanceId) {
    return parsedRecordToQuickMarcConverter.convert(srmClient.getParsedRecordByInstanceId(instanceId.toString()));
  }

  @Override
  public void updateById(UUID instanceId, QuickMarc quickMarc) {
    validationService.validateIdsMatch(quickMarc, instanceId);
    ParsedRecordDto parsedRecordDto = quickMarcToParsedRecordConverter.convert(updateRecordTimestamp(quickMarc));
    srmClient.putParsedRecordByInstanceId(quickMarc.getParsedRecordDtoId(), parsedRecordDto);
  }

  @Override
  public CreationStatus getCreationStatusByQmRecordId(UUID qmRecordId) {
    validationService.validateQmRecordId(qmRecordId);
    return statusService.findById(qmRecordId).map(statusMapper::fromEntity)
      .orElseThrow(() -> new NotFoundException(String.format(RECORD_NOT_FOUND_MESSAGE, qmRecordId)));
  }

  @Override
  public CreationStatus createNewInstance(QuickMarc quickMarc) {
    validationService.validateUserId(folioExecutionContext);
    final var userId = folioExecutionContext.getUserId().toString();

    final var jobExecutionId = createJobExecution(userId);
    final var creationStatus = saveStatus(jobExecutionId);

    updateJobExecutionWithProfile(jobExecutionId);
    postRecordsToParse(quickMarc, jobExecutionId);
    completeImport(jobExecutionId, null, Boolean.TRUE);

    return creationStatus;
  }

  public String createJobExecution(String userId) {
    var jobExecutionDto = getDefaultJodExecutionDto(userId, jobExecutionProfileProperties);
    var jobExecutionResponse = srmClient.postJobExecution(jobExecutionDto);
    return jobExecutionResponse.getJobExecutions().get(0).getId();
  }

  private void updateJobExecutionWithProfile(String jobExecutionId) {
    srmClient.putJobProfileByJobExecutionId(jobExecutionId, getDefaultJobProfile(jobExecutionProfileProperties));
    final var status = getStatusInProgress();
    statusService.updateByJobExecutionId(UUID.fromString(jobExecutionId), status);
  }

  private void postRecordsToParse(QuickMarc quickMarc, String jobExecutionId) {
    clearFields(quickMarc);
    final var parsedRecordDto = quickMarcToParsedRecordConverter.convert(updateRecordTimestamp(quickMarc));
    final var jsonString = JsonUtils.objectToJsonString(Objects.requireNonNull(parsedRecordDto).getParsedRecord().getContent());
    final var initialRecord = new InitialRecord().withRecord(jsonString);
    completeImport(jobExecutionId, initialRecord, Boolean.FALSE);
  }

  private void completeImport(String jobExecutionId, InitialRecord record, Boolean isEmpty) {
    final var rawRecordDto = getRawRecordsBody(record, isEmpty);
    srmClient.postRawRecordsByJobExecutionId(jobExecutionId, rawRecordDto);
  }

  public CreationStatus saveStatus(String jobExecutionId ) {
    final var status = getStatusNew(jobExecutionId);
    final var recordCreationStatus = statusService.save(status);
    return statusMapper.fromEntity(recordCreationStatus);
  }

  private void clearFields(QuickMarc quickMarc) {
    final Predicate<QuickMarcFields> field999Predicate = qmFields -> qmFields.getTag().equals("999");
    final Predicate<QuickMarcFields> emptyContentPredicate =  qmFields -> {
      final var content = qmFields.getContent();
      return content instanceof String && Strings.isEmpty((String)content);
    };
    quickMarc.getFields()
      .removeIf(field999Predicate.or(emptyContentPredicate));
    quickMarc.setParsedRecordId(null);
    quickMarc.setParsedRecordDtoId(null);
    quickMarc.setInstanceId(null);
  }

  private QuickMarc updateRecordTimestamp(QuickMarc quickMarc) {
    final var currentTime = encodeToMarcDateTime(LocalDateTime.now());
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresentOrElse(field -> field.setContent(currentTime),
        () -> quickMarc.addFieldsItem(new QuickMarcFields().tag(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD).content(currentTime))
      );
    return quickMarc;
  }
}
