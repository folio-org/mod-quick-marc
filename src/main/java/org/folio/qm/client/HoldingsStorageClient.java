package org.folio.qm.client;

import org.folio.qm.client.model.HoldingsRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient("holdings-storage")
public interface HoldingsStorageClient {

  @GetMapping(value = "/holdings/{id}")
  HoldingsRecord getHoldingById(@PathVariable("id") String id);

  @PutMapping(value = "/holdings/{id}")
  void updateHolding(@PathVariable("id") String id, HoldingsRecord holdingsRecord);
}
