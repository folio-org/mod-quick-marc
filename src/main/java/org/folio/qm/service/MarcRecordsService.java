package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

public interface MarcRecordsService {

  /**
   * This method retrieves QuickMarc record from SRS by corresponding external id.
   *
   * @param externalId external id
   * @return {@link QuickMarcView} record
   */
  QuickMarcView findByExternalId(UUID externalId);

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
   * @param updateResult   result to set handlers on it
   */
  void updateById(UUID parsedRecordId, QuickMarcEdit quickMarc, DeferredResult<ResponseEntity<Void>> updateResult);

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
  CreationStatus createNewRecord(QuickMarcCreate quickMarc);

  /**
   * This method make a proxy request to the 'mod-entities-links' endpoint.
   *
   * @param quickMarcView QuickMarc object
   * @return {@link QuickMarcView} with suggested links
   */
  QuickMarcView suggestLinks(QuickMarcView quickMarcView, AuthoritySearchParameter authoritySearchParameter,
                             Boolean ignoreAutoLinkingEnabled);
}

