package org.folio.qm.client;

import org.folio.qm.client.model.Instance;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "instance-storage")
public interface InstanceStorageClient {

  @GetMapping(value = "/instances/{id}")
  Instance getInstanceById(@PathVariable("id") String id);

  @PutMapping(value = "/instances/{id}")
  void updateInstance(@PathVariable("id") String id, @RequestBody Instance instance);
}
