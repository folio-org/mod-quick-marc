package org.folio.qm.validation;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;

public abstract class FieldValidationRule implements ValidationRule {

  @Override
  public Optional<ValidationError> validate(QuickMarc record) {
    return validate(record.getFields());
  }

  protected abstract Optional<ValidationError> validate(List<FieldItem> fieldItems);

  protected List<FieldItem> filterFieldsByTagCodePattern(List<FieldItem> fieldItems, Pattern tagCodePattern) {
    var matchPredicate = tagCodePattern.asMatchPredicate();
    return fieldItems.stream()
      .filter(fieldItem -> matchPredicate.test(fieldItem.getTag()))
      .collect(Collectors.toList());
  }

  protected BiFunction<String, List<FieldItem>, Optional<ValidationError>> onlyOneRequiredCondition() {
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

  protected BiFunction<String, List<FieldItem>, Optional<ValidationError>> atLeastOneRequiredCondition() {
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
