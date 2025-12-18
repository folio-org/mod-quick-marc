package org.folio.qm.client;

import java.util.UUID;
import org.folio.qm.client.model.InitJobExecutionsRqDto;
import org.folio.qm.client.model.InitJobExecutionsRsDto;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.ProfileInfo;
import org.folio.qm.client.model.RawRecordsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "change-manager")
public interface ChangeManagerClient {

  @PutMapping(value = "/parsedRecords/{id}")
  void putParsedRecordByInstanceId(@PathVariable("id") UUID id, ParsedRecordDto recordDto);

  @PostMapping(value = "/jobExecutions", produces = MediaType.APPLICATION_JSON_VALUE)
  InitJobExecutionsRsDto postJobExecution(@RequestBody InitJobExecutionsRqDto jobExecutionDto);

  @PutMapping(value = "/jobExecutions/{jobExecutionId}/jobProfile", produces = MediaType.APPLICATION_JSON_VALUE)
  void putJobProfileByJobExecutionId(@PathVariable("jobExecutionId") UUID jobExecutionId,
                                     @RequestBody ProfileInfo jobProfile);

  @PostMapping(value = "/jobExecutions/{jobExecutionId}/records",
    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  void postRawRecordsByJobExecutionId(@PathVariable("jobExecutionId") UUID jobExecutionId,
                                      @RequestBody RawRecordsDto rawRecords);
}
