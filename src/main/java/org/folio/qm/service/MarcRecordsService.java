package org.folio.qm.service;

import java.util.UUID;

import org.folio.qm.domain.dto.QuickMarc;

public interface MarcRecordsService {

  /**
   * This method returns QuickMarcJson record from SRS by corresponding instance's id
   *
   * @param instanceId instance's id
   * @return {@link QuickMarc} record
   */
  QuickMarc findByInstanceId(UUID instanceId);

  /**
   * This method updates QuickMarcJson record
   *
   * @param instanceId DTO id
   * @param quickMarc  QuickMarcJson object
   * @return {@link QuickMarc} record
   */
  void updateById(UUID instanceId, QuickMarc quickMarc);
}

