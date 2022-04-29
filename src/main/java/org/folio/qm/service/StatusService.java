package org.folio.qm.service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import org.folio.qm.domain.entity.ActionStatus;
import org.folio.qm.domain.entity.ActionStatusUpdate;

public interface StatusService {

  Optional<ActionStatus> findById(UUID qmRecordId);

  Optional<ActionStatus> findByJobExecutionId(UUID jobExecutionId);

  boolean updateByJobExecutionId(UUID jobExecutionId, ActionStatusUpdate statusUpdate);

  ActionStatus save(ActionStatus status);

  void removeOlderThan(Timestamp timestamp);
}
