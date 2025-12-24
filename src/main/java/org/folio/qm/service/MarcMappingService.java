package org.folio.qm.service;

import org.folio.qm.domain.FolioRecord;
import org.folio.qm.domain.QuickMarcRecord;

public interface MarcMappingService<T extends FolioRecord> {

  T mapNewRecord(QuickMarcRecord qmRecord);

  T mapUpdatedRecord(QuickMarcRecord qmRecord, T folioRecord);
}
