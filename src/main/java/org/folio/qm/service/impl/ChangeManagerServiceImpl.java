package org.folio.qm.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.ChangeManagerClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.domain.dto.InitJobExecutionsRqDto;
import org.folio.qm.domain.dto.InitJobExecutionsRsDto;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.ProfileInfo;
import org.folio.qm.domain.dto.RawRecordsDto;
import org.folio.qm.domain.dto.SourceRecord;
import org.folio.qm.service.ChangeManagerService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeManagerServiceImpl implements ChangeManagerService {

  private final ChangeManagerClient srmClient;
  private final SourceStorageClient storageClient;

  @Override
  public ParsedRecordDto getParsedRecordByExternalId(String externalId) {
    return srmClient.getParsedRecordByExternalId(externalId);
  }

  @Override
  public SourceRecord getSourceRecordByExternalId(String externalId) {
    return storageClient.getSourceRecord(externalId, SourceStorageClient.IdType.EXTERNAL);
  }

  @Override
  public void putParsedRecordByInstanceId(UUID id, ParsedRecordDto recordDto) {
    srmClient.putParsedRecordByInstanceId(id, recordDto);
  }

  @Override
  public InitJobExecutionsRsDto postJobExecution(InitJobExecutionsRqDto jobExecutionDto) {
    return srmClient.postJobExecution(jobExecutionDto);
  }

  @Override
  public void putJobProfileByJobExecutionId(UUID jobExecutionId, ProfileInfo jobProfile) {
    srmClient.putJobProfileByJobExecutionId(jobExecutionId, jobProfile);
  }

  @Override
  public void postRawRecordsByJobExecutionId(UUID jobExecutionId, RawRecordsDto rawRecords) {
    srmClient.postRawRecordsByJobExecutionId(jobExecutionId, rawRecords);
  }
}
