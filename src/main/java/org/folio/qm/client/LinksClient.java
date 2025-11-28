package org.folio.qm.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "links", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface LinksClient {

  @GetExchange(value = "/instances/{instanceId}")
  Optional<InstanceLinks> fetchLinksByInstanceId(@PathVariable("instanceId") UUID instanceId);

  @PutExchange("/instances/{instanceId}")
  void putLinksByInstanceId(@PathVariable("instanceId") UUID instanceId, @RequestBody InstanceLinks instanceLinks);

  record InstanceLinks(List<InstanceLink> links, Integer totalRecords) {
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
    String status;
    String errorCause;
  }
}
