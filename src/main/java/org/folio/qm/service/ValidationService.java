package org.folio.qm.service;

import java.util.UUID;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.spring.FolioExecutionContext;

public interface ValidationService {

  void validateIdsMatch(QuickMarc quickMarc, UUID instanceId);

  void validateQmRecordId(UUID qmRecordId);

  void validateTokenHeaderExists(FolioExecutionContext folioExecutionContext);
}
