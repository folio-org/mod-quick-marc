package org.folio.qm.validation.impl.holdings;

import static org.folio.qm.converter.elements.Constants.TAG_008_CONTROL_FIELD;

import java.util.List;
import java.util.Optional;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;
import org.springframework.stereotype.Component;

@Component
public class OnlyOne008ControlFieldValidationRule extends AbstractHoldingsValidationRule {

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith008TagCode = filterFieldsByTagCode(fieldItems, TAG_008_CONTROL_FIELD);
    return onlyOneRequiredCondition().apply(TAG_008_CONTROL_FIELD, fieldsWith008TagCode);
  }
}
