package org.folio.qm.validation.impl.holdings;

import static org.folio.qm.converter.elements.Constants.GENERAL_INFORMATION_CONTROL_FIELD;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;

@Component
public class OnlyOne008ControlFieldValidationRule extends AbstractHoldingsValidationRule {

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith008TagCode = filterFieldsByTagCode(fieldItems, GENERAL_INFORMATION_CONTROL_FIELD);
    return onlyOneRequiredCondition().apply(GENERAL_INFORMATION_CONTROL_FIELD, fieldsWith008TagCode);
  }
}
