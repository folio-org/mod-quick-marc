package org.folio.qm.service.storage.source;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.Record;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.client.model.SourceRecordSnapshot;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SourceRecordServiceImpl implements SourceRecordService {

  private final SourceStorageClient sourceStorageClient;

  @Override
  public Record get(UUID id) {
    return sourceStorageClient.getSourceRecord(id)
      .orElseThrow(() -> new NotFoundException(String.format("The source record was not found by id: %s", id)));
  }

  @Override
  public SourceRecord getByExternalId(UUID externalId) {
    return sourceStorageClient.getSourceRecord(externalId, SourceStorageClient.IdType.EXTERNAL)
      .orElseThrow(() ->
        new NotFoundException(String.format("The source record was not found by externalId: %s", externalId)));
  }

  @Override
  public Record create(Record sourceRecord) {
    var snapshot = SourceRecordSnapshot.snapshot();
    var createdSnapshot = sourceStorageClient.createSnapshot(snapshot);
    log.debug("createSrsRecord:: Snapshot created with id: {}", createdSnapshot.jobExecutionId());
    sourceRecord.setSnapshotId(createdSnapshot.jobExecutionId().toString());
    var createdRecord = sourceStorageClient.createSourceRecord(sourceRecord);
    log.debug("createSrsRecord:: SRS record created with id: {}", createdRecord.getId());
    return createdRecord;
  }

  @Override
  public void update(UUID id, Record sourceRecord) {
    sourceStorageClient.updateSourceRecord(id, sourceRecord);
  }
}
