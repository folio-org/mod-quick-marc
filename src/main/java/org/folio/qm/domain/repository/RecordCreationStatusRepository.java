package org.folio.qm.domain.repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordCreationStatusRepository extends JpaRepository<RecordCreationStatus, UUID> {

  Optional<RecordCreationStatus> findByJobExecutionId(UUID jobExecutionId);

  @Modifying
  @Query("delete RecordCreationStatus where updatedAt<:timestamp")
  void deleteByUpdatedAtBefore(@Param("timestamp") Timestamp timestamp);

}
