package org.folio.qm.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.Length;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class JobProfile {

  @Id
  @GeneratedValue
  private UUID id;

  @NotNull
  @Column(name = "profile_id", nullable = false)
  private UUID profileId;

  @NotNull
  @Length(max = 255)
  @Column(name = "profile_name", nullable = false)
  private String profileName;

  @NotNull
  @Column(name = "profile_action", nullable = false)
  @Enumerated(EnumType.STRING)
  private JobProfileAction profileAction;

  @NotNull
  @Column(name = "record_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private RecordType recordType;

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    JobProfile that = (JobProfile) o;
    return id != null && Objects.equals(id, that.id);
  }
}
