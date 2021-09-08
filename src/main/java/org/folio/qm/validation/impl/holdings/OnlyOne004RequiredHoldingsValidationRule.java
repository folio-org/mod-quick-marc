package org.folio.qm.validation.impl.holdings;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;

@Component
public class OnlyOne004RequiredHoldingsValidationRule extends AbstractHoldingsValidationRule {

  private static final String TAG_CODE = "004";

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith004TagCode = filterFieldsByTagCode(fieldItems, TAG_CODE);
    return onlyOneRequiredCondition().apply(TAG_CODE, fieldsWith004TagCode);
  }
}
