package org.folio.qm.service.storage.folio;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.HoldingsStorageClient;
import org.folio.qm.domain.HoldingsRecord;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolioRecordHoldingsService implements FolioRecordService<HoldingsRecord> {

  private final HoldingsStorageClient storageClient;

  @Override
  public HoldingsRecord get(UUID id) {
    return storageClient.getHoldingById(id)
      .orElseThrow(() -> new NotFoundException(String.format("Authority record with id: %s not found", id)));
  }

  @Override
  public HoldingsRecord create(HoldingsRecord folioRecord) {
    return storageClient.createHolding(folioRecord);
  }

  @Override
  public void update(UUID id, HoldingsRecord folioRecord) {
    storageClient.updateHolding(id, folioRecord);
  }
}
