package org.folio.qm.service;

import java.util.UUID;

import org.folio.qm.domain.dto.QuickMarc;

public interface ValidationService {

  void validateIdsMatch(QuickMarc quickMarc, UUID instanceId);

  void validateQmRecordId(UUID qmRecordId);
}
