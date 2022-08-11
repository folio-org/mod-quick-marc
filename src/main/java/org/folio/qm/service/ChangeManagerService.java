package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.domain.dto.InitJobExecutionsRqDto;
import org.folio.qm.domain.dto.InitJobExecutionsRsDto;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.ProfileInfo;
import org.folio.qm.domain.dto.RawRecordsDto;

public interface ChangeManagerService {

  ParsedRecordDto getParsedRecordByExternalId(String externalId);

  void putParsedRecordByInstanceId(UUID id, ParsedRecordDto recordDto);

  InitJobExecutionsRsDto postJobExecution(InitJobExecutionsRqDto jobExecutionDto);

  void putJobProfileByJobExecutionId(UUID jobExecutionId, ProfileInfo jobProfile);

  void postRawRecordsByJobExecutionId(UUID jobExecutionId, RawRecordsDto rawRecords);

}
