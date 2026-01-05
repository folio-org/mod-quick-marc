package org.folio.qm.service.validation;

import java.util.List;
import java.util.Optional;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.BaseQuickMarcRecord;

public interface ValidationRule {

  Optional<ValidationError> validate(BaseQuickMarcRecord qmRecord);

  boolean supportFormat(MarcFormat marcFormat);

  default ValidationError createValidationError(String tagCode, String message) {
    return new ValidationError(tagCode, message);
  }

  default List<FieldItem> filterFieldsByTagCode(List<FieldItem> fieldItems, String tagCode) {
    return fieldItems.stream()
      .filter(fieldItem -> tagCode.equals(fieldItem.getTag()))
      .toList();
  }
}
