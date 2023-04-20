package org.folio.qm.service;

import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;

public interface LinksService {

  void setRecordLinks(QuickMarcView qmRecord);

  void updateRecordLinks(QuickMarcEdit qmRecord);
}
