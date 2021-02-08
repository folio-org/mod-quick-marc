package org.folio.qm.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import org.folio.qm.domain.entity.RecordCreationStatus;

public interface RecordCreationStatusRepository extends JpaRepository<RecordCreationStatus, UUID> {

}
