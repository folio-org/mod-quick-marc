package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.validation.ValidationResult;

public interface ValidationService {

  ValidationResult validate(BaseMarcRecord quickMarc);

  void validateIdsMatch(QuickMarcEdit quickMarc, UUID externalId);

}
