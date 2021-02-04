package org.folio.qm.client;

import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "change-manager")
public interface SRMChangeManagerClient {

  @GetMapping(value = "/parsedRecords", produces = MediaType.APPLICATION_JSON_VALUE)
  ParsedRecordDto getParsedRecordByInstanceId(@RequestParam("instanceId") String instanceId);

  @PostMapping(value = "/parsedRecords")
  void postParsedRecord(@RequestBody  ParsedRecordDto recordDto);

  @PutMapping(value = "/parsedRecords/{id}")
  void putParsedRecordByInstanceId(@PathVariable("id") String id, ParsedRecordDto recordDto);

}
