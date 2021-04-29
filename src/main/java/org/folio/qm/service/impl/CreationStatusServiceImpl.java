package org.folio.qm.service.impl;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;
import org.folio.qm.domain.repository.RecordCreationStatusRepository;
import org.folio.qm.service.CreationStatusService;

@Service
@RequiredArgsConstructor
public class CreationStatusServiceImpl implements CreationStatusService {

  private final RecordCreationStatusRepository statusRepository;

  @Override
  public Optional<RecordCreationStatus> findById(UUID id) {
    return statusRepository.findById(id);
  }

  @Override
  public boolean updateByJobExecutionId(UUID jobExecutionId, RecordCreationStatusUpdate statusUpdate) {
    return statusRepository.update(statusUpdate, jobExecutionId) > 0;
  }

  @Override
  public RecordCreationStatus save(RecordCreationStatus status) {
    return statusRepository.saveAndFlush(status);
  }
}
