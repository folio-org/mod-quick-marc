package org.folio.qm.validation;

import java.util.List;
import java.util.Optional;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;

public abstract class RecordValidationRule implements ValidationRule {

  @Override
  public Optional<ValidationError> validate(QuickMarc qmRecord) {
    return validate(qmRecord.getFields(), qmRecord.getLeader());
  }

  protected abstract Optional<ValidationError> validate(List<FieldItem> fields, String leader);
}
