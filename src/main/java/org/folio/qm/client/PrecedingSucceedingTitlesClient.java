package org.folio.qm.client;

import org.folio.qm.client.model.instance.PrecedingSucceedingTitleCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "preceding-succeeding-titles")
public interface PrecedingSucceedingTitlesClient {

  @PutMapping(value = "/instances/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Void> updateTitles(@PathVariable("id") String id, PrecedingSucceedingTitleCollection titles);
}
