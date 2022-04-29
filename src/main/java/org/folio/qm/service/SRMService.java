package org.folio.qm.service;

import java.util.UUID;

import org.folio.qm.domain.dto.InitJobExecutionsRqDto;
import org.folio.qm.domain.dto.InitJobExecutionsRsDto;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.ProfileInfo;
import org.folio.qm.domain.dto.RawRecordsDto;

public interface SRMService {

  ParsedRecordDto getParsedRecordByExternalId(String externalId);

  InitJobExecutionsRsDto postJobExecution(InitJobExecutionsRqDto jobExecutionDto);

  void putJobProfileByJobExecutionId(UUID jobExecutionId, ProfileInfo jobProfile);

  void postRawRecordsByJobExecutionId(UUID jobExecutionId, RawRecordsDto rawRecords);

}
