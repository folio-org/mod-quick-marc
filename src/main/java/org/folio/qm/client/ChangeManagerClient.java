package org.folio.qm.client;

import java.util.UUID;
import org.folio.qm.client.model.InitJobExecutionsRqDto;
import org.folio.qm.client.model.InitJobExecutionsRsDto;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.ProfileInfo;
import org.folio.qm.client.model.RawRecordsDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "change-manager", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface ChangeManagerClient {

  @PutExchange(value = "/parsedRecords/{id}")
  void putParsedRecordByInstanceId(@PathVariable("id") UUID id, @RequestBody ParsedRecordDto recordDto);

  @PostExchange(value = "/jobExecutions")
  InitJobExecutionsRsDto postJobExecution(@RequestBody InitJobExecutionsRqDto jobExecutionDto);

  @PutExchange(value = "/jobExecutions/{jobExecutionId}/jobProfile")
  void putJobProfileByJobExecutionId(@PathVariable("jobExecutionId") UUID jobExecutionId,
                                     @RequestBody ProfileInfo jobProfile);

  @PostExchange(value = "/jobExecutions/{jobExecutionId}/records")
  void postRawRecordsByJobExecutionId(@PathVariable("jobExecutionId") UUID jobExecutionId,
                                      @RequestBody RawRecordsDto rawRecords);
}
