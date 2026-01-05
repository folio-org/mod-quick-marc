package org.folio.qm.client;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.model.AuthorityRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "authority-storage")
public interface AuthorityStorageClient {

  @GetMapping(value = "/authorities/{id}")
  Optional<AuthorityRecord> getAuthorityById(@PathVariable UUID id);

  @PutMapping(value = "/authorities/{id}")
  void updateAuthority(@PathVariable UUID id, AuthorityRecord authority);

  @PostMapping(value = "/authorities")
  AuthorityRecord createAuthority(AuthorityRecord authority);
}
