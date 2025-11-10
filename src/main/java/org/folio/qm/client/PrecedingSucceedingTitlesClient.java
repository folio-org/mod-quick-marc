package org.folio.qm.client;

import org.folio.qm.client.model.PrecedingSucceedingTitleCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "preceding-succeeding-titles")
public interface PrecedingSucceedingTitlesClient {

  @PutMapping(value = "/instances/{id}")
  void updateTitles(@PathVariable("id") String id, PrecedingSucceedingTitleCollection titles);
}
