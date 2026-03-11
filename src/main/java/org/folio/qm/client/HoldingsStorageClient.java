package org.folio.qm.client;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.model.HoldingsRecord;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "holdings-storage", accept = MediaType.APPLICATION_JSON_VALUE)
public interface HoldingsStorageClient {

  @GetExchange(value = "/holdings/{id}")
  Optional<HoldingsRecord> getHoldingById(@PathVariable("id") UUID id);

  @PostExchange(value = "/holdings")
  HoldingsRecord createHolding(@RequestBody HoldingsRecord holdingsRecord);

  @PutExchange(value = "/holdings/{id}")
  void updateHolding(@PathVariable("id") UUID id, @RequestBody HoldingsRecord holdingsRecord);
}
