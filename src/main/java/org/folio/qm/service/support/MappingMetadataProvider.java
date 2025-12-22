package org.folio.qm.service.support;

import static org.apache.commons.lang3.StringUtils.isBlank;

import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.qm.client.MappingMetadataClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MappingMetadataProvider {

  private final MappingMetadataClient client;

  @Cacheable(cacheNames = "mapping-metadata-cache",
    key = "@folioExecutionContext.tenantId + #recordType",
    unless = "#result == null")
  public MappingData getMappingData(String recordType) {
    log.trace("getMappingData:: fetching mapping metadata");
    try {
      var metadata = client.getMappingMetadata(recordType);
      if (metadata == null || isBlank(metadata.mappingParams()) || isBlank(metadata.mappingRules())) {
        log.warn("Failed to fetch mapping metadata");
        return null;
      }
      return new MappingData(new JsonObject(metadata.mappingRules()),
        new JsonObject(metadata.mappingParams()).mapTo(MappingParameters.class));
    } catch (Exception ex) {
      log.warn("Failed to fetch mapping metadata, reason: {}", ex.getMessage());
      return null;
    }
  }

  public record MappingData(JsonObject mappingRules, MappingParameters mappingParameters) {
  }
}
