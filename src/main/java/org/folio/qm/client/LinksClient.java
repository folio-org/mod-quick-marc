package org.folio.qm.client;

import static org.folio.qm.config.CacheNames.QM_FETCH_LINKING_RULES_RESULTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "links", dismiss404 = true)
public interface LinksClient {

  @GetMapping(value = "/instances/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  Optional<InstanceLinks> fetchLinksByInstanceId(@PathVariable("instanceId") UUID instanceId);

  @PutMapping("/instances/{instanceId}")
  void putLinksByInstanceId(@PathVariable("instanceId") UUID instanceId, InstanceLinks instanceLinks);

  @Cacheable(cacheNames = QM_FETCH_LINKING_RULES_RESULTS,
    key = "@folioExecutionContext.tenantId",
    unless = "#result.isEmpty()")
  @GetMapping(value = "/linking-rules/instance-authority", produces = MediaType.APPLICATION_JSON_VALUE)
  List<LinkingRuleDto> fetchLinkingRules();

  @Value
  class InstanceLinks {

    List<InstanceLink> links;
    Integer totalRecords;
  }

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  @AllArgsConstructor
  class InstanceLink {

    Integer id;
    UUID authorityId;
    String authorityNaturalId;
    UUID instanceId;
    Integer linkingRuleId;
  }

  @Data
  class LinkingRuleDto {
    private Integer id;
    private String bibField;
    private String authorityField;
    private List<String> authoritySubfields = new ArrayList();
    private List<SubfieldModification> subfieldModifications = new ArrayList();
    private String bibRecordTag;

    private SubfieldValidation validation;
  }

  class SubfieldModification {
    private String source;
    private String target;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
  }

  class SubfieldValidation {
    private List<Map<String, Boolean>> existence = null;
  }
}
