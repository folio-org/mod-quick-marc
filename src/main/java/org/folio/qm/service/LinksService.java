package org.folio.qm.service;

import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.QuickMarcRecord;

public interface LinksService {

  void setRecordLinks(QuickMarcView qmRecord);

  void updateRecordLinks(QuickMarcRecord qmRecord);
}
