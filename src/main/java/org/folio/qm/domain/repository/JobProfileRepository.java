package org.folio.qm.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;

public interface JobProfileRepository extends JpaRepository<JobProfile, UUID> {

  Optional<JobProfile> findByProfileActionAndRecordType(JobProfileAction profileAction, RecordType recordType);

}
