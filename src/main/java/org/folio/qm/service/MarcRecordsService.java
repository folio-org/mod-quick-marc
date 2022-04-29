package org.folio.qm.service;

import java.util.UUID;

import org.folio.qm.domain.dto.ActionStatusDto;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.ActionStatusDto;

public interface MarcRecordsService {

  /**
   * Retrieves QuickMarc record from 'mod-source-record-manager' by corresponding external id
   *
   * @param externalId external id
   * @return {@link QuickMarc} record
   */
  QuickMarc findRecordByExternalId(UUID externalId);

  /**
   * Triggers the 'mod-source-record-manager' create import process of QuickMarc record
   *
   * @param quickMarc QuickMarc object
   * @return {@link ActionStatusDto}
   */
  ActionStatusDto createRecord(QuickMarc quickMarc);

  /**
   * Triggers the 'mod-source-record-manager' update import process of QuickMarc record
   *
   * @param parsedRecordId record id
   * @param quickMarc      QuickMarc object
   * @return {@link ActionStatusDto}
   */
  ActionStatusDto updateById(UUID parsedRecordId, QuickMarc quickMarc);

  /**
   * Triggers the 'mod-source-record-manager' delete import process of QuickMarc record
   *
   * @param externalId external id
   * @return {@link ActionStatusDto}
   */
  ActionStatusDto deleteRecordByExternalId(UUID externalId);

  /**
   * Retrieves status of record action
   *
   * @param actionId id of record action
   * @return {@link ActionStatusDto}
   */
  ActionStatusDto getActionStatusByActionId(UUID actionId);
}

