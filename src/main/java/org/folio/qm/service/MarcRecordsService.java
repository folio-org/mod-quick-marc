package org.folio.qm.service;

import java.util.UUID;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;

public interface MarcRecordsService {

  /**
   * This method returns QuickMarc record from SRS by corresponding instance's id
   *
   * @param instanceId instance's id
   * @return {@link QuickMarc} record
   */
  QuickMarc findByInstanceId(UUID instanceId);

  /**
   * This method updates QuickMarc record
   *
   * @param instanceId DTO id
   * @param quickMarc  QuickMarc object
   * @return {@link QuickMarc} record
   */
  void updateById(UUID instanceId, QuickMarc quickMarc);

  /**
   * This method retrieves status of new record creation
   *
   * @param qmRecordId id of record that should be created
   * @return {@link CreationStatus}
   */
  CreationStatus getCreationStatusByQmRecordId(UUID qmRecordId);

  /**
   * This method triggers the 'mod-source-record-manager' import process of QuickMarc record
   *
   * @param quickMarc  QuickMarc object
   * @return {@link UUID} qmParsedRecordId of record
   */
  CreationStatus createNewInstance(QuickMarc quickMarc);
}

