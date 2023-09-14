package org.folio.qm.service;

import org.folio.qm.domain.entity.MarcSpecification;
import org.folio.qm.domain.entity.RecordType;

public interface MarcSpecificationsService {

  /**
   * This method retrieves Marc Specification JSON record from SRS by corresponding recordType and fieldTag name.
   *
   * @param recordType  Marc format
   * @param fieldTag field Tag
   * @return {@link String} record
   */
  MarcSpecification findByMarcFormatAndFieldTag(RecordType recordType, String fieldTag);
}

