package org.folio.qm.service.storage.folio;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.AuthorityStorageClient;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FolioRecordAuthorityService implements FolioRecordService<AuthorityRecord> {

  private final AuthorityStorageClient storageClient;

  @Override
  public AuthorityRecord get(UUID id) {
    log.debug("get:: Retrieving authority record by id: {}", id);
    return storageClient.getAuthorityById(id)
      .orElseThrow(() -> {
        log.error("get:: Authority record not found with id: {}", id);
        return new NotFoundException(String.format("Authority record with id: %s not found", id));
      });
  }

  @Override
  public AuthorityRecord create(AuthorityRecord folioRecord) {
    log.debug("create:: Creating authority record");
    var created = storageClient.createAuthority(folioRecord);
    log.info("create:: Authority record created with id: {}", created.getId());
    return created;
  }

  @Override
  public void update(UUID id, AuthorityRecord folioRecord) {
    log.debug("update:: Updating authority record with id: {}", id);
    storageClient.updateAuthority(id, folioRecord);
    log.info("update:: Authority record updated successfully with id: {}", id);
  }
}
