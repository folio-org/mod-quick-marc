package org.folio.qm.client;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.rest.jaxrs.model.Instances;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "instance-storage")
public interface InstanceStorageClient {

  @GetMapping(value = "/instances/{id}")
  Optional<InstanceRecord> getInstanceById(@PathVariable UUID id);

  @GetMapping(value = "/instances?query=hrid=={id}&limit=1&offset=0")
  Instances getInstances(@PathVariable String id);

  @PostMapping(value = "/instances")
  InstanceRecord createInstance(@RequestBody InstanceRecord instance);

  @PutMapping(value = "/instances/{id}")
  void updateInstance(@PathVariable UUID id, @RequestBody InstanceRecord instance);
}
