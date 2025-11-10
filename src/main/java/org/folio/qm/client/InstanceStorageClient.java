package org.folio.qm.client;

import java.util.List;
import org.folio.qm.client.model.Instance;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "instance-storage")
public interface InstanceStorageClient {

  @GetMapping(value = "/instances/{id}")
  Instance getInstanceById(@PathVariable("id") String id);

  @GetMapping(value = "/instances?query=hrid=={id}&limit=10&offset=0")
  InstanceResult getInstanceIdByHrid(@PathVariable("id") String id);

  @PutMapping(value = "/instances/{id}")
  ResponseEntity<Void> updateInstance(@PathVariable("id") String id, @RequestBody Instance instance);

  @PostMapping(value = "/instances")
  ResponseEntity<Instance> createInstance(@RequestBody Instance instance);

  record InstanceResult(List<Instance> instances, int totalRecords) {
    public String getInstanceId() {
      if (instances != null && totalRecords == 1) {
        var instance = instances.getFirst();
        return instance != null ? instance.getId() : null;
      }
      return null;
    }
  }
}
