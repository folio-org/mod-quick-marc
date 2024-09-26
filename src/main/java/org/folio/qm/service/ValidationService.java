package org.folio.qm.service;

import java.util.List;
import java.util.UUID;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidationIssue;
import org.folio.qm.validation.ValidationResult;

public interface ValidationService {

  ValidationResult validate(BaseMarcRecord quickMarc);

  List<ValidationIssue> validate(ValidatableRecord validatableRecord);

  void validateIdsMatch(QuickMarcEdit quickMarc, UUID externalId);

  void validateMarcRecord(BaseMarcRecord marcRecord, boolean is001RequiredField);
}
