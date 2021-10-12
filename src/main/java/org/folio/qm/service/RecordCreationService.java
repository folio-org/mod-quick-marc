package org.folio.qm.service;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;

public interface RecordCreationService {

  CreationStatus createRecord(QuickMarc quickMarc);
}
