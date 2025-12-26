package org.folio.qm.service.validation.impl.common;

import static org.folio.qm.convertion.elements.Constants.TAG_008_CONTROL_FIELD;
import static org.folio.qm.domain.dto.MarcFormat.AUTHORITY;
import static org.folio.qm.domain.dto.MarcFormat.BIBLIOGRAPHIC;
import static org.folio.qm.domain.dto.MarcFormat.HOLDINGS;

import java.util.List;
import java.util.Optional;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.validation.FieldValidationRule;
import org.folio.qm.service.validation.ValidationError;
import org.springframework.stereotype.Component;

@Component
public class OnlyOne008ControlFieldValidationRule extends FieldValidationRule {

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith008TagCode = filterFieldsByTagCode(fieldItems, TAG_008_CONTROL_FIELD);
    return onlyOneRequiredCondition().apply(TAG_008_CONTROL_FIELD, fieldsWith008TagCode);
  }

  /**
   * This rule is applicable to all available marc formats.
   */
  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return BIBLIOGRAPHIC == marcFormat || HOLDINGS == marcFormat || AUTHORITY == marcFormat;
  }
}
