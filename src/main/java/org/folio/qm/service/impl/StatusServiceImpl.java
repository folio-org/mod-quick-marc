package org.folio.qm.service.impl;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.folio.qm.domain.entity.ActionStatus;
import org.folio.qm.domain.entity.ActionStatusUpdate;
import org.folio.qm.domain.repository.ActionStatusRepository;
import org.folio.qm.service.StatusService;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

  private final ActionStatusRepository statusRepository;

  @Override
  public Optional<ActionStatus> findById(UUID id) {
    return statusRepository.findById(id);
  }

  @Override
  public Optional<ActionStatus> findByJobExecutionId(UUID jobExecutionId) {
    return statusRepository.findByJobExecutionId(jobExecutionId);
  }

  @Override
  @Transactional
  public boolean updateByJobExecutionId(UUID jobExecutionId, ActionStatusUpdate statusUpdate) {
    return findByJobExecutionId(jobExecutionId)
      .map(recordCreationStatus -> {
        recordCreationStatus.setStatus(statusUpdate.getStatus());
        recordCreationStatus.setExternalId(statusUpdate.getExternalId());
        recordCreationStatus.setMarcId(statusUpdate.getMarcId());
        recordCreationStatus.setErrorMessage(statusUpdate.getErrorMessage());
        return true;
      }).orElse(false);
  }

  @Override
  public ActionStatus save(ActionStatus status) {
    return statusRepository.saveAndFlush(status);
  }

  @Override
  @Transactional
  public void removeOlderThan(Timestamp timestamp) {
    statusRepository.deleteByUpdatedAtBefore(timestamp);
  }
}
