package org.folio.qm.service;

import java.util.UUID;

import org.folio.qm.domain.dto.QuickMarc;

public interface ValidationService {

  void validateIds(QuickMarc quickMarc, UUID instanceId);
}
