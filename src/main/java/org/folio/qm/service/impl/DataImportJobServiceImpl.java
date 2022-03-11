package org.folio.qm.service.impl;

import static org.folio.qm.service.DataImportJobService.getDefaultJodExecutionDto;
import static org.folio.qm.service.DataImportJobService.toJobProfileInfo;
import static org.folio.qm.service.DataImportJobService.toLastRawRecordsDto;
import static org.folio.qm.service.DataImportJobService.toRawRecordsDto;
import static org.folio.qm.util.StatusUtils.getStatusInProgress;
import static org.folio.qm.util.StatusUtils.getStatusNew;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.DataImportJobService;
import org.folio.qm.service.JobProfileService;
import org.folio.spring.FolioExecutionContext;

@Component
@RequiredArgsConstructor
public class DataImportJobServiceImpl implements DataImportJobService {

  private static final Map<ParsedRecordDto.RecordTypeEnum, RecordType> typeMap = Map.of(
    ParsedRecordDto.RecordTypeEnum.AUTHORITY, RecordType.MARC_AUTHORITY,
    ParsedRecordDto.RecordTypeEnum.BIB, RecordType.MARC_BIBLIOGRAPHIC,
    ParsedRecordDto.RecordTypeEnum.HOLDING, RecordType.MARC_HOLDINGS
  );

  private final SRMChangeManagerClient srmClient;
  private final CreationStatusService statusService;
  private final JobProfileService jobProfileService;
  private final FolioExecutionContext folioExecutionContext;

  @Override
  public UUID executeDataImportJob(ParsedRecordDto recordDto, JobProfileAction action) {
    var userId = folioExecutionContext.getUserId();
    var jobProfile = getJobProfile(recordDto, action);

    var jobId = initJob(jobProfile, userId);

    updateJobProfile(jobId, jobProfile);
    sendRecord(recordDto, jobId);
    completeImport(jobId);

    return jobId;
  }

  private UUID initJob(JobProfile jobProfile, UUID userId) {
    var jobExecutionDto = getDefaultJodExecutionDto(userId, jobProfile);
    var jobExecutionResponse = srmClient.postJobExecution(jobExecutionDto);
    var jobExecutionId = jobExecutionResponse.getJobExecutions().get(0).getId();
    saveNewStatus(jobExecutionId);
    return jobExecutionId;
  }

  private void updateJobProfile(UUID jobExecutionId, JobProfile jobProfile) {
    srmClient.putJobProfileByJobExecutionId(jobExecutionId, toJobProfileInfo(jobProfile));
    saveInProgressStatus(jobExecutionId);
  }

  private void sendRecord(ParsedRecordDto parsedRecordDto, UUID jobExecutionId) {
    srmClient.postRawRecordsByJobExecutionId(jobExecutionId, toRawRecordsDto(parsedRecordDto));
  }

  private void completeImport(UUID jobExecutionId) {
    srmClient.postRawRecordsByJobExecutionId(jobExecutionId, toLastRawRecordsDto());
  }

  private JobProfile getJobProfile(ParsedRecordDto recordDto, JobProfileAction action) {
    return jobProfileService.getJobProfile(typeMap.get(recordDto.getRecordType()), action);
  }

  private void saveNewStatus(UUID jobExecutionId) {
    var status = getStatusNew(jobExecutionId);
    statusService.save(status);
  }

  private void saveInProgressStatus(UUID jobExecutionId) {
    final var status = getStatusInProgress();
    statusService.updateByJobExecutionId(jobExecutionId, status);
  }
}
