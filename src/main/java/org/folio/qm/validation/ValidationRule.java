package org.folio.qm.validation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;

public interface ValidationRule {

  Optional<ValidationError> validate(QuickMarc record);

  boolean supportFormat(MarcFormat marcFormat);

  default ValidationError createValidationError(String tagCode, String message) {
    return new ValidationError(tagCode, message);
  }

  default List<FieldItem> filterFieldsByTagCode(List<FieldItem> fieldItems, String tagCode) {
    return fieldItems.stream()
      .filter(fieldItem -> tagCode.equals(fieldItem.getTag()))
      .collect(Collectors.toList());
  }
}
