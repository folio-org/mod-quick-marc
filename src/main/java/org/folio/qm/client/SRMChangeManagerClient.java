package org.folio.qm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.folio.rest.jaxrs.model.ParsedRecordDto;

@FeignClient(value = "change-manager")
public interface SRMChangeManagerClient {

  @GetMapping(value = "/change-manager/parsedRecords", produces = MediaType.APPLICATION_JSON_VALUE)
  ParsedRecordDto getParsedRecordByInstanceId(@RequestParam("instanceId") String instanceId);

  @PutMapping(value = "/change-manager/parsedRecords/{id}")
  void putParsedRecordByInstanceId(@PathVariable("id") String id, ParsedRecordDto recordDto);

}
