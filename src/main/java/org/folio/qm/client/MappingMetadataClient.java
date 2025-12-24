package org.folio.qm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mapping-metadata")
public interface MappingMetadataClient {

  @GetMapping(value = "/type/{recordType}")
  MappingMetadata getMappingMetadata(@PathVariable String recordType);

  record MappingMetadata(String mappingRules, String mappingParams) {
  }
}
