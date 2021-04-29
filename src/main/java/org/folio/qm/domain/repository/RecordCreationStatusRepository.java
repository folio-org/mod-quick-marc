package org.folio.qm.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import org.folio.qm.domain.entity.RecordCreationStatus;

public interface RecordCreationStatusRepository extends JpaRepository<RecordCreationStatus, UUID> {

  Optional<RecordCreationStatus> findByJobExecutionId(UUID jobExecutionId);

}
