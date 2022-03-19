package org.folio.qm.service.impl;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;
import org.folio.qm.domain.repository.RecordCreationStatusRepository;
import org.folio.qm.service.StatusService;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

  private final RecordCreationStatusRepository statusRepository;

  @Override
  public Optional<RecordCreationStatus> findById(UUID id) {
    return statusRepository.findById(id);
  }

  @Override
  public Optional<RecordCreationStatus> findByJobExecutionId(UUID jobExecutionId) {
    return statusRepository.findByJobExecutionId(jobExecutionId);
  }

  @Override
  @Transactional
  public boolean updateByJobExecutionId(UUID jobExecutionId, RecordCreationStatusUpdate statusUpdate) {
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
  public RecordCreationStatus save(RecordCreationStatus status) {
    return statusRepository.saveAndFlush(status);
  }

  @Override
  @Transactional
  public void removeOlderThan(Timestamp timestamp) {
    statusRepository.deleteByUpdatedAtBefore(timestamp);
  }
}
