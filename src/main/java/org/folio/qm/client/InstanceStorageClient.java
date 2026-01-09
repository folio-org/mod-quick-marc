package org.folio.qm.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.model.InstanceRecord;
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

  @GetMapping(value = "/instances?query=hrid=={id}&limit=10&offset=0")
  InstanceResult getInstanceByHrid(@PathVariable String id);

  @PostMapping(value = "/instances")
  InstanceRecord createInstance(@RequestBody InstanceRecord instance);

  @PutMapping(value = "/instances/{id}")
  void updateInstance(@PathVariable UUID id, @RequestBody InstanceRecord instance);

  record InstanceResult(List<InstanceRecord> instances, int totalRecords) {
    public String getInstanceId() {
      if (instances != null && totalRecords == 1) {
        var instance = instances.getFirst();
        return instance != null ? instance.getId() : null;
      }
      return null;
    }
  }
}
