package org.folio.qm.service.mapping;

import org.folio.qm.domain.model.FolioRecord;
import org.folio.qm.domain.model.QuickMarcRecord;

public interface MarcMappingService<T extends FolioRecord> {

  T mapNewRecord(QuickMarcRecord qmRecord);

  T mapUpdatedRecord(QuickMarcRecord qmRecord, T folioRecord);
}
