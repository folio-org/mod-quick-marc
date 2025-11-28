package org.folio.qm.client;

import java.util.Optional;
import org.folio.qm.client.model.SourceRecord;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "source-storage", accept = MediaType.APPLICATION_JSON_VALUE)
public interface SourceStorageClient {

  @GetExchange(value = "/source-records/{id}")
  Optional<SourceRecord> getSourceRecord(@PathVariable("id") String id, @RequestParam("idType") IdType idType);

  enum IdType {
    EXTERNAL
  }
}
