package org.folio.qm.service;

import org.folio.qm.domain.entity.MarcSpecification;
import org.folio.qm.domain.entity.RecordType;

public interface MarcSpecificationsService {

  /**
   * This method retrieves Marc Specification JSON record from SRS by corresponding recordType and fieldTag name.
   *
   * @param recordType  Record type
   * @param fieldTag Field Tag
   * @return {@link String} record
   */
  MarcSpecification findByRecordTypeAndFieldTag(RecordType recordType, String fieldTag);
}

