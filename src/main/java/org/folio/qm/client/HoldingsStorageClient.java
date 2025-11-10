package org.folio.qm.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.folio.qm.domain.entity.HoldingsRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient("holdings-storage")
public interface HoldingsStorageClient {

  @GetMapping(value = "/holdings/{id}", produces = APPLICATION_JSON_VALUE)
  HoldingsRecord getHoldingById(@PathVariable("id") String id);

  @PutMapping(value = "/holdings/{id}", produces = APPLICATION_JSON_VALUE)
  void updateHolding(@PathVariable("id") String id, HoldingsRecord holdingsRecord);
}
