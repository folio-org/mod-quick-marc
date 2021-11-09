package org.folio.qm.validation;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

public interface FieldValidationRule {

  Optional<ValidationError> validate(List<FieldItem> fieldItems);

  boolean supportFormat(MarcFormat marcFormat);

  default ValidationError createValidationError(String tagCode, String message) {
    return new ValidationError(tagCode, message);
  }

  default List<FieldItem> filterFieldsByTagCode(List<FieldItem> fieldItems, String tagCode) {
    return fieldItems.stream()
      .filter(fieldItem -> tagCode.equals(fieldItem.getTag()))
      .collect(Collectors.toList());
  }

  default List<FieldItem> filterFieldsByTagCodePattern(List<FieldItem> fieldItems, Pattern tagCodePattern) {
    var matchPredicate = tagCodePattern.asMatchPredicate();
    return fieldItems.stream()
      .filter(fieldItem -> matchPredicate.test(fieldItem.getTag()))
      .collect(Collectors.toList());
  }

  default BiFunction<String, List<FieldItem>, Optional<ValidationError>> onlyOneRequiredCondition() {
    return (tagCode, fields) -> {
      if (fields.isEmpty()) {
        return Optional.of(createValidationError(tagCode, "Is required tag"));
      } else if (fields.size() != 1) {
        return Optional.of(createValidationError(tagCode, "Is unique tag"));
      } else if (fields.get(0).getContent() instanceof CharSequence && StringUtils.isEmpty((CharSequence) fields.get(0).getContent())) {
        return Optional.of(createValidationError(tagCode, "Content couldn't be empty"));
      } else {
        return Optional.empty();
      }
    };
  }

  default BiFunction<String, List<FieldItem>, Optional<ValidationError>> atLeastOneRequiredCondition() {
    return (tagCode, fields) -> {
      if (fields.isEmpty()) {
        return Optional.of(createValidationError(tagCode, "Is required tag"));
      } else {
        for (FieldItem fieldItem : fields) {
          if (StringUtils.isEmpty((CharSequence) fieldItem.getContent())) {
            return Optional.of(createValidationError(tagCode, "Content couldn't be empty"));
          }
        }
      }
      return Optional.empty();
    };
  }
}
