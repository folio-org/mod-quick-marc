package org.folio.qm.domain.repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.folio.qm.domain.entity.ActionStatus;

public interface ActionStatusRepository extends JpaRepository<ActionStatus, UUID> {

  String ACTION_STATUS_GRAPH = "action-status-graph";

  @Override
  @EntityGraph(value = ACTION_STATUS_GRAPH)
  Optional<ActionStatus> findById(UUID uuid);

  @EntityGraph(value = ACTION_STATUS_GRAPH)
  Optional<ActionStatus> findByJobExecutionId(UUID jobExecutionId);

  @Modifying
  @Query("delete ActionStatus where updatedAt<:timestamp")
  void deleteByUpdatedAtBefore(@Param("timestamp") Timestamp timestamp);

}
