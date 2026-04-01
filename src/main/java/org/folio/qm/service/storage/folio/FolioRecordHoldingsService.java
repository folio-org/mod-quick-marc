package org.folio.qm.service.storage.folio;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.HoldingsStorageClient;
import org.folio.qm.domain.model.HoldingsFolioRecord;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FolioRecordHoldingsService implements FolioRecordService<HoldingsFolioRecord> {

  private final HoldingsStorageClient storageClient;

  @Override
  public HoldingsFolioRecord get(UUID id) {
    log.debug("get:: Retrieving holdings record by id: {}", id);
    return storageClient.getHoldingById(id)
      .orElseThrow(() -> {
        log.error("get:: Holdings record not found with id: {}", id);
        return new NotFoundException(String.format("Holdings record with id: %s not found", id));
      });
  }

  @Override
  public HoldingsFolioRecord create(HoldingsFolioRecord folioRecord) {
    log.debug("create:: Creating holdings record");
    var created = storageClient.createHolding(folioRecord);
    log.info("create:: Holdings record created with id: {}", created.getId());
    return created;
  }

  @Override
  public void update(UUID id, HoldingsFolioRecord folioRecord) {
    log.debug("update:: Updating holdings record with id: {}", id);
    storageClient.updateHolding(id, folioRecord);
    log.info("update:: Holdings record updated successfully with id: {}", id);
  }
}
