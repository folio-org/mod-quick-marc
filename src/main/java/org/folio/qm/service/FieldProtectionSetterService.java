package org.folio.qm.service;

import org.folio.qm.domain.dto.QuickMarc;

public interface FieldProtectionSetterService {

  QuickMarc applyFieldProtection(QuickMarc qmRecord);
}
