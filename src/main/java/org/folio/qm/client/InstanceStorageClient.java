package org.folio.qm.client;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.model.InstanceFolioRecord;
import org.folio.rest.jaxrs.model.Instances;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "instance-storage")
public interface InstanceStorageClient {

  @GetExchange(value = "/instances/{id}")
  Optional<InstanceFolioRecord> getInstanceById(@PathVariable("id") UUID id);

  @GetExchange(value = "/instances?query=hrid=={id}&limit=1&offset=0")
  Instances getInstances(@PathVariable("id") String id);

  @PostExchange(value = "/instances")
  InstanceFolioRecord createInstance(@RequestBody InstanceFolioRecord instance);

  @PutExchange(value = "/instances/{id}")
  void updateInstance(@PathVariable("id") UUID id, @RequestBody InstanceFolioRecord instance);
}
