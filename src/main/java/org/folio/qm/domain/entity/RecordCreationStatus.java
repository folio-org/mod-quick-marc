package org.folio.qm.domain.entity;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class RecordCreationStatus {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private UUID jobExecutionId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Type(PostgreSQLEnumType.class)
  private RecordCreationStatusEnum status;

  private String errorMessage;

  private UUID externalId;

  private UUID marcId;

  @Column(nullable = false)
  private Timestamp createdAt;

  private Timestamp updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    RecordCreationStatus that = (RecordCreationStatus) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
