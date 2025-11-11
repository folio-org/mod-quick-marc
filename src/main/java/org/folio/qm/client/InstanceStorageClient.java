package org.folio.qm.client;

import java.util.Map;
import org.folio.qm.client.model.Instance;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "instance-storage")
public interface InstanceStorageClient {

  @GetMapping(value = "/instances/{id}")
  Instance getInstanceById(@PathVariable("id") String id);

  @PutMapping(value = "/instances/{id}", consumes = "application/json")
  ResponseEntity<Void> updateInstance(@PathVariable("id") String id, @RequestBody Map<String, Object> instance);
}
