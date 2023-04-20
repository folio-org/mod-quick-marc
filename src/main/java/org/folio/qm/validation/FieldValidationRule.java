package org.folio.qm.validation;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;

public abstract class FieldValidationRule implements ValidationRule {

  public static final String EMPTY_CONTENT_ERROR_MSG = "Content couldn't be empty";

  public static final String IS_UNIQUE_TAG_ERROR_MSG = "Is unique tag";

  public static final String IS_REQUIRED_TAG_ERROR_MSG = "Is required tag";

  @Override
  public Optional<ValidationError> validate(BaseMarcRecord qmRecord) {
    return validate(qmRecord.getFields());
  }

  protected abstract Optional<ValidationError> validate(List<FieldItem> fieldItems);

  protected List<FieldItem> filterFieldsByTagCodePattern(List<FieldItem> fieldItems, Pattern tagCodePattern) {
    var matchPredicate = tagCodePattern.asMatchPredicate();
    return fieldItems.stream()
      .filter(fieldItem -> matchPredicate.test(fieldItem.getTag()))
      .toList();
  }

  protected BiFunction<String, List<FieldItem>, Optional<ValidationError>> onlyOneRequiredCondition() {
    return (tagCode, fields) -> {
      if (fields.isEmpty()) {
        return Optional.of(createValidationError(tagCode, IS_REQUIRED_TAG_ERROR_MSG));
      } else if (fields.size() != 1) {
        return Optional.of(createValidationError(tagCode, IS_UNIQUE_TAG_ERROR_MSG));
      } else if (fields.get(0).getContent() instanceof CharSequence charSequence && StringUtils.isEmpty(charSequence)) {
        return Optional.of(createValidationError(tagCode, EMPTY_CONTENT_ERROR_MSG));
      } else {
        return Optional.empty();
      }
    };
  }

  protected BiFunction<String, List<FieldItem>, Optional<ValidationError>> notRequiredOnlyOneCondition() {
    return (tagCode, fields) -> {
      if (fields.isEmpty()) {
        return Optional.empty();
      } else if (fields.size() != 1) {
        return Optional.of(createValidationError(tagCode, IS_UNIQUE_TAG_ERROR_MSG));
      } else if (fields.get(0).getContent() instanceof CharSequence charSequence && StringUtils.isEmpty(charSequence)) {
        return Optional.of(createValidationError(tagCode, EMPTY_CONTENT_ERROR_MSG));
      }

      return Optional.empty();
    };
  }

  protected BiFunction<Pair<String, Integer>, List<FieldItem>, Optional<ValidationError>> contentLengthCondition() {
    return (tagLengthPair, fields) -> {
      var tagCode = tagLengthPair.getLeft();
      var contentSize = tagLengthPair.getRight();
      for (FieldItem field : fields) {
        if (field.getContent() instanceof Map && ((Map<?, ?>) field.getContent()).size() != contentSize) {
          var simplifiedContent = ((Map<?, ?>) field.getContent()).values().stream()
            .map(o -> {
              if (o instanceof List) {
                return StringUtils.join((List<?>) o, "");
              } else {
                return o.toString();
              }
            })
            .collect(Collectors.joining());
          if (simplifiedContent.length() != contentSize) {
            return Optional.of(createValidationError(tagCode, format("Content of %s field has wrong length", tagCode)));
          }
        }
      }
      return Optional.empty();
    };
  }
}
