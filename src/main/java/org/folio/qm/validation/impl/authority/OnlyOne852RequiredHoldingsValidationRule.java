package org.folio.qm.validation.impl.authority;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;
import org.springframework.stereotype.Component;

@Component
public class OnlyOne852RequiredHoldingsValidationRule extends AbstractAuthorityValidationRule {

  private static final Pattern TAG_CODE_PATTERN = Pattern.compile("852");

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith852Tag = filterFieldsByTagCodePattern(fieldItems, TAG_CODE_PATTERN);
    return onlyOneRequiredCondition().apply("852", fieldsWith852Tag);
  }
}
