package org.folio.qm.service;

import java.util.Optional;
import java.util.UUID;

import org.folio.qm.domain.entity.RecordCreationStatus;

public interface CreationStatusService {

  Optional<RecordCreationStatus> getCreationStatusById(UUID qmRecordId);

  Optional<RecordCreationStatus> findByJobExecutionId(UUID jobExecutionId);
}
