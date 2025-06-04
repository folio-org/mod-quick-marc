package org.folio.qm.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.entity.MarcSpecification;
import org.folio.qm.domain.entity.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcSpecificationRepository extends JpaRepository<MarcSpecification, UUID> {

  Optional<MarcSpecification> findByRecordTypeAndFieldTag(RecordType recordType, String fieldTag);
}
