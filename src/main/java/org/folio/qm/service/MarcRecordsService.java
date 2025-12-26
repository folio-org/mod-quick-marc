package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;

public interface MarcRecordsService {

  /**
   * This method updates QuickMarc record.
   *
   * @param parsedRecordId DTO id
   * @param quickMarc      QuickMarc object
   */
  void updateById(UUID parsedRecordId, QuickMarcEdit quickMarc);

  /**
   * This method triggers the 'mod-source-record-manager' import process of QuickMarc record.
   *
   * @param quickMarc QuickMarc object
   * @return {@link UUID} qmParsedRecordId of record
   */
  QuickMarcView createRecord(QuickMarcCreate quickMarc);
}

