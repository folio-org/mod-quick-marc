package org.folio.qm.service.impl;

import static org.folio.qm.service.DataImportJobService.prepareJodExecutionDto;
import static org.folio.qm.service.DataImportJobService.toJobProfileInfo;
import static org.folio.qm.service.DataImportJobService.toRawRecordsDto;
import static org.folio.qm.util.StatusUtils.getStatusInProgress;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.service.DataImportJobService;
import org.folio.qm.service.JobProfileService;
import org.folio.qm.service.SRMService;
import org.folio.qm.service.StatusService;
import org.folio.spring.FolioExecutionContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportJobServiceImpl implements DataImportJobService {

  private static final Map<ParsedRecordDto.RecordTypeEnum, RecordType> typeMap = Map.of(
    ParsedRecordDto.RecordTypeEnum.AUTHORITY, RecordType.MARC_AUTHORITY,
    ParsedRecordDto.RecordTypeEnum.BIB, RecordType.MARC_BIBLIOGRAPHIC,
    ParsedRecordDto.RecordTypeEnum.HOLDING, RecordType.MARC_HOLDINGS
  );

  private final StatusService statusService;
  private final SRMService srmService;
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
    var jobExecutionDto = prepareJodExecutionDto(jobProfile, userId);
    var jobExecutionResponse = srmService.postJobExecution(jobExecutionDto);
    return jobExecutionResponse.getJobExecutions().get(0).getId();
  }

  private void updateJobProfile(UUID jobExecutionId, JobProfile jobProfile) {
    srmService.putJobProfileByJobExecutionId(jobExecutionId, toJobProfileInfo(jobProfile));
    saveInProgressStatus(jobExecutionId, jobProfile.getId());
  }

  private void sendRecord(ParsedRecordDto parsedRecordDto, UUID jobExecutionId) {
    srmService.postRawRecordsByJobExecutionId(jobExecutionId, toRawRecordsDto(parsedRecordDto));
  }

  private void completeImport(UUID jobExecutionId) {
    srmService.postRawRecordsByJobExecutionId(jobExecutionId, DataImportJobService.toLastRawRecordsDto());
  }

  private JobProfile getJobProfile(ParsedRecordDto recordDto, JobProfileAction action) {
    return jobProfileService.getJobProfile(typeMap.get(recordDto.getRecordType()), action);
  }

  private void saveInProgressStatus(UUID jobExecutionId, UUID jobProfileId) {
    var status = getStatusInProgress(jobExecutionId, jobProfileId);
    statusService.save(status);
  }

}
