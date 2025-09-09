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
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.service.ChangeManagerService;
import org.folio.qm.service.DataImportJobService;
import org.folio.qm.service.JobProfileService;
import org.folio.qm.service.StatusService;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataImportJobServiceImpl implements DataImportJobService {

  private static final Map<RecordTypeEnum, RecordType> TYPE_MAP = Map.of(
    RecordTypeEnum.AUTHORITY, RecordType.MARC_AUTHORITY,
    RecordTypeEnum.BIB, RecordType.MARC_BIBLIOGRAPHIC,
    RecordTypeEnum.HOLDING, RecordType.MARC_HOLDINGS
  );

  private final ChangeManagerService changeManagerService;
  private final StatusService statusService;
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
    var jobExecutionResponse = changeManagerService.postJobExecution(jobExecutionDto);
    var jobExecutionId = jobExecutionResponse.getJobExecutions().getFirst().getId();
    saveNewStatus(jobExecutionId);
    return jobExecutionId;
  }

  private void updateJobProfile(UUID jobExecutionId, JobProfile jobProfile) {
    changeManagerService.putJobProfileByJobExecutionId(jobExecutionId, toJobProfileInfo(jobProfile));
    saveInProgressStatus(jobExecutionId);
  }

  private void sendRecord(ParsedRecordDto parsedRecordDto, UUID jobExecutionId) {
    changeManagerService.postRawRecordsByJobExecutionId(jobExecutionId, toRawRecordsDto(parsedRecordDto));
  }

  private void completeImport(UUID jobExecutionId) {
    changeManagerService.postRawRecordsByJobExecutionId(jobExecutionId, toLastRawRecordsDto());
  }

  private JobProfile getJobProfile(ParsedRecordDto recordDto, JobProfileAction action) {
    return jobProfileService.getJobProfile(TYPE_MAP.get(recordDto.getRecordType()), action);
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
