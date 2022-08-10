package org.folio.qm.client;

import java.util.UUID;
import org.folio.qm.domain.dto.InitJobExecutionsRqDto;
import org.folio.qm.domain.dto.InitJobExecutionsRsDto;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.ProfileInfo;
import org.folio.qm.domain.dto.RawRecordsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "change-manager")
public interface ChangeManagerClient {

  @GetMapping(value = "/parsedRecords", produces = MediaType.APPLICATION_JSON_VALUE)
  ParsedRecordDto getParsedRecordByExternalId(@RequestParam("externalId") String externalId);

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
