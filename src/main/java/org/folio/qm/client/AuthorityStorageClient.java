package org.folio.qm.client;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.model.AuthorityRecord;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "authority-storage", accept = MediaType.APPLICATION_JSON_VALUE)
public interface AuthorityStorageClient {

  @GetExchange(value = "/authorities/{id}")
  Optional<AuthorityRecord> getAuthorityById(@PathVariable("id") UUID id);

  @PutExchange(value = "/authorities/{id}")
  void updateAuthority(@PathVariable("id") UUID id, @RequestBody AuthorityRecord authority);

  @PostExchange(value = "/authorities")
  AuthorityRecord createAuthority(@RequestBody AuthorityRecord authority);
}
