package org.folio.qm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import org.folio.rest.jaxrs.model.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.InitJobExecutionsRsDto;
import org.folio.rest.jaxrs.model.JobExecution;
import org.folio.rest.jaxrs.model.JobProfileInfo;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.folio.rest.jaxrs.model.RawRecordsDto;


@FeignClient(value = "change-manager")
public interface SRMChangeManagerClient {

  @GetMapping(value = "/parsedRecords", produces = MediaType.APPLICATION_JSON_VALUE)
  ParsedRecordDto getParsedRecordByInstanceId(@RequestParam("instanceId") String instanceId);

  @PutMapping(value = "/parsedRecords/{id}")
  void putParsedRecordByInstanceId(@PathVariable("id") String id, ParsedRecordDto recordDto);

  @PostMapping(value = "/jobExecutions", produces = MediaType.APPLICATION_JSON_VALUE)
  InitJobExecutionsRsDto postJobExecution(@RequestBody InitJobExecutionsRqDto jobExecutionDto);

  @PutMapping(value = "/jobExecutions/{jobExecutionId}/jobProfile", produces = MediaType.APPLICATION_JSON_VALUE)
  JobExecution putJobProfileByJobExecutionId(@PathVariable("jobExecutionId") String jobExecutionId, @RequestBody JobProfileInfo jobProfile);

  @PostMapping(value = "/jobExecutions/{jobExecutionId}/records", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  void postRawRecordsByJobExecutionId(@PathVariable("jobExecutionId") String jobExecutionId, @RequestBody RawRecordsDto rawRecords);
}
