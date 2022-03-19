package org.folio.qm.service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;

public interface StatusService {

  Optional<RecordCreationStatus> findById(UUID qmRecordId);

  Optional<RecordCreationStatus> findByJobExecutionId(UUID jobExecutionId);

  boolean updateByJobExecutionId(UUID jobExecutionId, RecordCreationStatusUpdate statusUpdate);

  RecordCreationStatus save(RecordCreationStatus status);

  void removeOlderThan(Timestamp timestamp);
}
