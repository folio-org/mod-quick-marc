package org.folio.qm.validation.impl.authority;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;

@Component
public class OnlyOne1XXRequiredAuthorityValidationRule extends AbstractAuthorityValidationRule {

  private static final Pattern TAG_CODE_PATTERN = Pattern.compile("^1\\d{2}$");

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith1XXTag = filterFieldsByTagCodePattern(fieldItems, TAG_CODE_PATTERN);
    return onlyOneRequiredCondition().apply("1XX", fieldsWith1XXTag);
  }
}
