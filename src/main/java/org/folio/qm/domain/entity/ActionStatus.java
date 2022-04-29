package org.folio.qm.domain.entity;

import static org.folio.qm.domain.repository.ActionStatusRepository.ACTION_STATUS_GRAPH;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedEntityGraph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NamedEntityGraph(name = ACTION_STATUS_GRAPH, includeAllAttributes = true)
public class ActionStatus {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private UUID jobExecutionId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ActionStatusEnum status;

  private String errorMessage;

  private UUID externalId;

  private UUID marcId;

  @Column(nullable = false)
  private Timestamp createdAt;

  private Timestamp updatedAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "job_profile_id", nullable = false)
  private JobProfile jobProfile;

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) { return false; }
    ActionStatus that = (ActionStatus) o;
    return id != null && Objects.equals(id, that.id);
  }
}
