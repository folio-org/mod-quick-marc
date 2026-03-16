package org.folio.qm.service.storage.source;

import org.folio.qm.domain.dto.QuickMarcView;

public interface FieldProtectionSetterService {

  void applyFieldProtection(QuickMarcView qmRecord);
}
