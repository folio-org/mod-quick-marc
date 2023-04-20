package org.folio.qm.service;

import org.folio.qm.domain.dto.QuickMarcView;

public interface FieldProtectionSetterService {

  void applyFieldProtection(QuickMarcView qmRecord);
}
