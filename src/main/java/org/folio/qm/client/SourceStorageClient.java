package org.folio.qm.client;

import org.folio.qm.domain.dto.SourceRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "source-storage")
public interface SourceStorageClient {

  @GetMapping(value = "/source-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  SourceRecord getSourceRecord(@PathVariable("id") String id, @RequestParam("idType") IdType idType);

  enum IdType {
    EXTERNAL,
    RECORD,
    SRS_RECORD
  }
}
