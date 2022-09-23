package org.folio.qm.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "links", decode404 = true)
public interface LinksClient {

  @GetMapping(value = "/instances/{instanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  Optional<InstanceLinks> fetchLinksByInstanceId(@PathVariable("instanceId") UUID instanceId);

  @Value
  class InstanceLinks {

    List<InstanceLink> links;
    Integer totalRecords;
  }

  @Value
  class InstanceLink {

    Integer id;
    UUID authorityId;
    String authorityNaturalId;
    UUID instanceId;
    String bibRecordTag;
    List<String> bibRecordSubfields;
  }
}
