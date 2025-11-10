package org.folio.qm.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mapping-metadata")
public interface MappingMetadataClient {

  @GetMapping(value = "/type/{recordType}", produces = APPLICATION_JSON_VALUE)
  MappingMetadata getMappingMetadata(@PathVariable("recordType") String recordType);

  record MappingMetadata(String mappingRules, String mappingParams) {
  }
}
