package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

public interface MarcRecordsService {

  /**
   * This method retrieves QuickMarc record from SRS by corresponding external id.
   *
   * @param externalId external id
   * @return {@link QuickMarc} record
   */
  QuickMarc findByExternalId(UUID externalId);

  /**
   * This method deletes QuickMarc record by its id.
   *
   * @param externalId external id
   * @return {@link DeferredResult} response
   */
  CreationStatus deleteByExternalId(UUID externalId);

  /**
   * This method updates QuickMarc record.
   *
   * @param parsedRecordId DTO id
   * @param quickMarc      QuickMarc object
   * @param updateResult result to set handlers on it
   */
  void updateById(UUID parsedRecordId, QuickMarc quickMarc, DeferredResult<ResponseEntity<Void>> updateResult);

  /**
   * This method retrieves status of new record creation.
   *
   * @param qmRecordId id of record that should be created
   * @return {@link CreationStatus}
   */
  CreationStatus getCreationStatusByQmRecordId(UUID qmRecordId);

  /**
   * This method triggers the 'mod-source-record-manager' import process of QuickMarc record.
   *
   * @param quickMarc QuickMarc object
   * @return {@link UUID} qmParsedRecordId of record
   */
  CreationStatus createNewRecord(QuickMarc quickMarc);
}

