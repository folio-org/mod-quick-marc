package org.folio.qm.service.impl;

import lombok.RequiredArgsConstructor;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.service.ChangeManagerService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeManagerServiceImpl implements ChangeManagerService {

  private final SourceStorageClient storageClient;

  @Override
  public SourceRecord getSourceRecordByExternalId(String externalId) {
    return storageClient.getSourceRecord(externalId, SourceStorageClient.IdType.EXTERNAL);
  }
}
