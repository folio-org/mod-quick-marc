package org.folio.qm.service.storage.folio;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.AuthorityStorageClient;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolioRecordAuthorityService implements FolioRecordService<AuthorityRecord> {

  private final AuthorityStorageClient storageClient;

  @Override
  public AuthorityRecord get(UUID id) {
    return storageClient.getAuthorityById(id)
      .orElseThrow(() -> new NotFoundException(String.format("Authority record with id: %s not found", id)));
  }

  @Override
  public AuthorityRecord create(AuthorityRecord folioRecord) {
    return storageClient.createAuthority(folioRecord);
  }

  @Override
  public void update(UUID id, AuthorityRecord folioRecord) {
    storageClient.updateAuthority(id, folioRecord);
  }
}
