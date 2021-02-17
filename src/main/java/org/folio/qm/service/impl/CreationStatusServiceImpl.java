package org.folio.qm.service.impl;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.repository.RecordCreationStatusRepository;
import org.folio.qm.service.CreationStatusService;

@Service
@RequiredArgsConstructor
public class CreationStatusServiceImpl implements CreationStatusService {

  private final RecordCreationStatusRepository statusRepository;

  @Override
  public Optional<RecordCreationStatus> getCreationStatusById(UUID id) {
    return statusRepository.findById(id);
  }

  @Override
  public Optional<RecordCreationStatus> findByJobExecutionId(UUID jobExecutionId) {
    return statusRepository.findByJobExecutionId(jobExecutionId);
  }
}
