package org.folio.qm.service;

import java.util.Optional;
import java.util.UUID;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;

public interface CreationStatusService {

  Optional<RecordCreationStatus> findById(UUID qmRecordId);

  Optional<RecordCreationStatus> findByJobExecutionId(UUID jobExecutionId);

  boolean updateByJobExecutionId(UUID jobExecutionId, RecordCreationStatusUpdate statusUpdate);
}
