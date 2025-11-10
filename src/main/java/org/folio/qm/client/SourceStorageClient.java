package org.folio.qm.client;

import org.folio.Record;
import org.folio.qm.client.model.SourceRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "source-storage")
public interface SourceStorageClient {

  @GetMapping(value = "/source-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  SourceRecord getSourceRecord(@PathVariable("id") String id, @RequestParam("idType") IdType idType);

  @GetMapping(value = "/records/{id}")
  Record getSrsRecord(@PathVariable("id") String id);

  @PutMapping(value = "/records/{id}/generation")
  Record updateSrsRecordGeneration(@PathVariable("id") String matchId, Record record);

  enum IdType {
    EXTERNAL
  }
}
