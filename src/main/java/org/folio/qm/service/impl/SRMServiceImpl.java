package org.folio.qm.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.domain.dto.InitJobExecutionsRqDto;
import org.folio.qm.domain.dto.InitJobExecutionsRsDto;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.ProfileInfo;
import org.folio.qm.domain.dto.RawRecordsDto;
import org.folio.qm.service.SRMService;

@Service
@RequiredArgsConstructor
public class SRMServiceImpl implements SRMService {

  private final SRMChangeManagerClient changeManagerClient;

  @Override
  public ParsedRecordDto getParsedRecordByExternalId(String externalId) {
    return changeManagerClient.getParsedRecordByExternalId(externalId);
  }

  @Override
  public InitJobExecutionsRsDto postJobExecution(InitJobExecutionsRqDto jobExecutionDto) {
    return changeManagerClient.postJobExecution(jobExecutionDto);
  }

  @Override
  public void putJobProfileByJobExecutionId(UUID jobExecutionId, ProfileInfo jobProfile) {
    changeManagerClient.putJobProfileByJobExecutionId(jobExecutionId, jobProfile);
  }

  @Override
  public void postRawRecordsByJobExecutionId(UUID jobExecutionId, RawRecordsDto rawRecords) {
    changeManagerClient.postRawRecordsByJobExecutionId(jobExecutionId, rawRecords);
  }

}
