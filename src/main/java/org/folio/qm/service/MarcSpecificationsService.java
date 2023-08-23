package org.folio.qm.service;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.entity.MarcSpecification;

public interface MarcSpecificationsService {

  /**
   * This method retrieves Marc Specification JSON record from SRS by corresponding recordType and fieldTag name.
   *
   * @param marcFormat Marc format
   * @param fieldTag field Tag
   * @return {@link String} record
   */
  MarcSpecification findByMarcFormatAndFieldTag(MarcFormat marcFormat, String fieldTag);
}

