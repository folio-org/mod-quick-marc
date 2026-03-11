package org.folio.qm.client;

import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitles;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "preceding-succeeding-titles", accept = MediaType.APPLICATION_JSON_VALUE)
public interface PrecedingSucceedingTitlesClient {

  @PutExchange(value = "/instances/{id}")
  void updateTitles(@PathVariable("id") String id, @RequestBody InstancePrecedingSucceedingTitles titles);
}
