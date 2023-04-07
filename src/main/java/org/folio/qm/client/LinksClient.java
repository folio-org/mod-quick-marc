package org.folio.qm.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
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
  }
}
