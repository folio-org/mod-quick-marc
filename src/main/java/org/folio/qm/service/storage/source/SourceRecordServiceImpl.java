package org.folio.qm.service.storage.source;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.Record;
import org.folio.qm.client.SourceStorageClient;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SourceRecordServiceImpl implements SourceRecordService {

  private final SourceStorageClient sourceStorageClient;

  @Override
  public Record get(UUID id) {
    log.debug("get:: Retrieving source record by id: {}", id);
    return sourceStorageClient.getSourceRecord(id)
      .orElseThrow(() -> {
        log.error("get:: Source record not found by id: {}", id);
        return new NotFoundException(String.format("The source record was not found by id: %s", id));
      });
  }

  @Override
  public Record getByExternalId(UUID externalId) {
    log.debug("getByExternalId:: Retrieving source record by externalId: {}", externalId);
    return sourceStorageClient.getSourceRecord(externalId, SourceStorageClient.IdType.EXTERNAL)
      .orElseThrow(() -> {
        log.error("getByExternalId:: Source record not found by externalId: {}", externalId);
        return new NotFoundException(String.format("The source record was not found by externalId: %s", externalId));
      });
  }

  @Override
  public Record create(Record sourceRecord) {
    log.debug("create:: Creating source record");
    var createdSnapshot = sourceStorageClient.createSnapshot(SourceStorageClient.SourceRecordSnapshot.snapshot());
    log.debug("create:: Snapshot created with id: {}", createdSnapshot.jobExecutionId());
    sourceRecord.setSnapshotId(createdSnapshot.jobExecutionId().toString());
    var createdRecord = sourceStorageClient.createSourceRecord(sourceRecord);
    log.info("create:: Source record created with id: {}", createdRecord.getId());
    return createdRecord;
  }

  @Override
  public void update(UUID id, Record sourceRecord) {
    log.debug("update:: Updating source record with id: {}", id);
    sourceStorageClient.updateSourceRecord(id, sourceRecord);
    log.info("update:: Source record updated successfully with id: {}", id);
  }
}
