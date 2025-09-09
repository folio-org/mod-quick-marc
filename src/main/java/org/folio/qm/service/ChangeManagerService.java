package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.client.model.InitJobExecutionsRqDto;
import org.folio.qm.client.model.InitJobExecutionsRsDto;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.ProfileInfo;
import org.folio.qm.client.model.RawRecordsDto;
import org.folio.qm.client.model.SourceRecord;

public interface ChangeManagerService {

  SourceRecord getSourceRecordByExternalId(String externalId);

  void putParsedRecordByInstanceId(UUID id, ParsedRecordDto recordDto);

  InitJobExecutionsRsDto postJobExecution(InitJobExecutionsRqDto jobExecutionDto);

  void putJobProfileByJobExecutionId(UUID jobExecutionId, ProfileInfo jobProfile);

  void postRawRecordsByJobExecutionId(UUID jobExecutionId, RawRecordsDto rawRecords);
}
