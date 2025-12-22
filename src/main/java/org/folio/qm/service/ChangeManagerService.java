package org.folio.qm.service;

import org.folio.qm.client.model.SourceRecord;

public interface ChangeManagerService {

  SourceRecord getSourceRecordByExternalId(String externalId);
}
