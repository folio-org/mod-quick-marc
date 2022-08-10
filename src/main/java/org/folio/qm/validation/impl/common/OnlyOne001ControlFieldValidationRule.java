package org.folio.qm.validation.impl.common;

import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.domain.dto.MarcFormat.AUTHORITY;
import static org.folio.qm.domain.dto.MarcFormat.BIBLIOGRAPHIC;
import static org.folio.qm.domain.dto.MarcFormat.HOLDINGS;

import java.util.List;
import java.util.Optional;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.FieldValidationRule;
import org.folio.qm.validation.ValidationError;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class OnlyOne001ControlFieldValidationRule extends FieldValidationRule {
  @Override
  protected Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith001TagCode = filterFieldsByTagCode(fieldItems, TAG_001_CONTROL_FIELD);
    if (CollectionUtils.isEmpty(fieldsWith001TagCode)) {
      return Optional.empty();
    } else {
      return onlyOneRequiredCondition().apply(TAG_001_CONTROL_FIELD, fieldsWith001TagCode);
    }
  }

  /**
   * This rule is applicable to all available marc formats.
   */
  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return BIBLIOGRAPHIC == marcFormat || HOLDINGS == marcFormat || AUTHORITY == marcFormat;
  }
}
