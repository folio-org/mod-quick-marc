package org.folio.qm.client;

import org.folio.Authority;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "authority-storage")
public interface AuthorityStorageClient {

  @GetMapping(value = "/authorities/{id}")
  Authority getAuthorityById(@PathVariable("id") String id);

  @PutMapping(value = "/authorities/{id}")
  void updateAuthority(@PathVariable("id") String id, Authority authority);
}
