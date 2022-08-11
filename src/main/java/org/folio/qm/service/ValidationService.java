package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.validation.ValidationResult;

public interface ValidationService {

  ValidationResult validate(QuickMarc quickMarc);

  void validateIdsMatch(QuickMarc quickMarc, UUID externalId);

}
