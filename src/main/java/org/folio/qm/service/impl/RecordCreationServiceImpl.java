package org.folio.qm.service.impl;

import static java.util.Objects.requireNonNull;

import static org.folio.qm.util.ChangeManagerPayloadUtils.getDefaultJobProfile;
import static org.folio.qm.util.ChangeManagerPayloadUtils.getDefaultJodExecutionDto;
import static org.folio.qm.util.ChangeManagerPayloadUtils.getRawRecordsBody;
import static org.folio.qm.util.ErrorUtils.buildError;
import static org.folio.qm.util.JsonUtils.objectToJsonString;
import static org.folio.qm.util.MarcUtils.updateRecordTimestamp;
import static org.folio.qm.util.StatusUtils.getStatusInProgress;
import static org.folio.qm.util.StatusUtils.getStatusNew;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.config.properties.JobExecutionProfileProperties;
import org.folio.qm.converter.MarcConverterFactory;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.RecordCreationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.rest.jaxrs.model.InitialRecord;
import org.folio.spring.FolioExecutionContext;

@Component
@RequiredArgsConstructor
public class RecordCreationServiceImpl implements RecordCreationService {

  private static final String FORMAT_NOT_SUPPORTED_MSG = "Creating record with this format is not supported";

  private final SRMChangeManagerClient srmClient;
  private final CreationStatusService statusService;
  private final JobExecutionProfileProperties jobExecutionProfileProperties;
  private final FolioExecutionContext folioExecutionContext;
  private final CreationStatusMapper statusMapper;
  private final MarcConverterFactory marcConverterFactory;

  @Override
  public CreationStatus createRecord(QuickMarc quickMarc) {
    final var userId = folioExecutionContext.getUserId().toString();

    var options = getProfileOptions(quickMarc)
      .orElseThrow(() -> new ValidationException(buildError(ErrorUtils.ErrorType.INTERNAL, FORMAT_NOT_SUPPORTED_MSG)));

    var jobExecutionId = createJobExecution(userId, options);
    var creationStatus = saveStatus(jobExecutionId);

    updateJobExecutionWithProfile(jobExecutionId, options);
    postRecordsToParse(quickMarc, jobExecutionId);
    completeImport(jobExecutionId, null, true);

    return creationStatus;
  }

  private Optional<JobExecutionProfileProperties.ProfileOptions> getProfileOptions(QuickMarc quickMarc) {
    JobExecutionProfileProperties.ProfileOptions options = null;
    if (MarcFormat.BIBLIOGRAPHIC == quickMarc.getMarcFormat()) {
      options = jobExecutionProfileProperties.getMarcBib();
    } else if (MarcFormat.HOLDINGS == quickMarc.getMarcFormat()) {
      options = jobExecutionProfileProperties.getMarcHoldings();
    }
    return Optional.ofNullable(options);
  }

  private String createJobExecution(String userId, JobExecutionProfileProperties.ProfileOptions options) {
    var jobExecutionDto = getDefaultJodExecutionDto(userId, options);
    var jobExecutionResponse = srmClient.postJobExecution(jobExecutionDto);
    return jobExecutionResponse.getJobExecutions().get(0).getId();
  }

  private CreationStatus saveStatus(String jobExecutionId) {
    final var status = getStatusNew(jobExecutionId);
    final var recordCreationStatus = statusService.save(status);
    return statusMapper.fromEntity(recordCreationStatus);
  }

  private void updateJobExecutionWithProfile(String jobExecutionId, JobExecutionProfileProperties.ProfileOptions options) {
    srmClient.putJobProfileByJobExecutionId(jobExecutionId, getDefaultJobProfile(options));
    final var status = getStatusInProgress();
    statusService.updateByJobExecutionId(UUID.fromString(jobExecutionId), status);
  }

  private void postRecordsToParse(QuickMarc quickMarc, String jobExecutionId) {
    clearFields(quickMarc);
    var converter = marcConverterFactory.findConverter(quickMarc.getMarcFormat());
    var parsedRecordDto = converter.convert(updateRecordTimestamp(quickMarc));
    var jsonString = objectToJsonString(requireNonNull(parsedRecordDto).getParsedRecord().getContent());
    var initialRecord = new InitialRecord().withRecord(jsonString);
    completeImport(jobExecutionId, initialRecord, Boolean.FALSE);
  }

  private void completeImport(String jobExecutionId, InitialRecord record, boolean isEmpty) {
    final var rawRecordDto = getRawRecordsBody(record, isEmpty);
    srmClient.postRawRecordsByJobExecutionId(jobExecutionId, rawRecordDto);
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
}
