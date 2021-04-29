package org.folio.qm.domain.repository;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;

public interface RecordCreationStatusRepository extends JpaRepository<RecordCreationStatus, UUID> {

  @Modifying
  @Transactional
  @Query("UPDATE RecordCreationStatus s SET "
    + "s.status =:#{#update.status}, "
    + "s.instanceId =:#{#update.instanceId}, "
    + "s.marcBibId =:#{#update.marcBibId}, "
    + "s.errorMessage =:#{#update.errorMessage} "
    + "WHERE s.jobExecutionId =:jobExecutionId"
  )
  int update(@Param("update") RecordCreationStatusUpdate update, @Param("jobExecutionId") UUID jobExecutionId);

}
