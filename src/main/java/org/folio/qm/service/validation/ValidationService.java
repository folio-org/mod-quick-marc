package org.folio.qm.service.validation;

import java.util.List;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidationIssue;
import org.folio.qm.domain.model.QuickMarcRecord;

public interface ValidationService {

  ValidationResult validate(BaseMarcRecord quickMarc);

  List<ValidationIssue> validate(ValidatableRecord validatableRecord);

  void validateMarcRecord(BaseMarcRecord marcRecord, List<SkippedValidationError> skippedValidationErrors);

  void validateMarcRecord(QuickMarcRecord qmRecord, List<SkippedValidationError> skippedValidationErrors);
}
