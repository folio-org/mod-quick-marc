package org.folio.qm.client;

import org.folio.Authority;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "authority-storage")
public interface AuthorityStorageClient {

  @GetMapping(value = "/authorities/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  Authority getAuthorityById(@PathVariable("id") String id);

  @PutMapping(value = "/authorities/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  void updateAuthority(@PathVariable("id") String id, Authority authority);
}
