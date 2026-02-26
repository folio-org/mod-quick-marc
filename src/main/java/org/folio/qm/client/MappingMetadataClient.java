package org.folio.qm.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "mapping-metadata", accept = MediaType.APPLICATION_JSON_VALUE)
public interface MappingMetadataClient {

  @GetExchange(value = "/type/{recordType}")
  MappingMetadata getMappingMetadata(@PathVariable("recordType") String recordType);

  record MappingMetadata(String mappingRules, String mappingParams) {
  }
}
