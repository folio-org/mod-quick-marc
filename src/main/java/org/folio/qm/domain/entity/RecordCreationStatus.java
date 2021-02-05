package org.folio.qm.domain.entity;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import org.hibernate.annotations.TypeDef;

import org.folio.spring.domain.PostgreEnumTypeSql;

@Data
@Entity
@TypeDef(
  name = "pgsql_enum",
  typeClass = PostgreEnumTypeSql.class
)
public class RecordCreationStatus {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private UUID jobExecutionId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private RecordCreationStatusEnum status;

  private UUID instanceId;

  @Column(nullable = false)
  private Timestamp createdAt;

  private Timestamp updatedAt;
}
