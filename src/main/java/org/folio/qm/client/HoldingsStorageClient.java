package org.folio.qm.client;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.model.HoldingsRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("holdings-storage")
public interface HoldingsStorageClient {

  @GetMapping(value = "/holdings/{id}")
  Optional<HoldingsRecord> getHoldingById(@PathVariable UUID id);

  @PostMapping(value = "/holdings")
  HoldingsRecord createHolding(@RequestBody HoldingsRecord holdingsRecord);

  @PutMapping(value = "/holdings/{id}")
  void updateHolding(@PathVariable UUID id, HoldingsRecord holdingsRecord);
}
