package org.folio.qm.validation.impl.holdings;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;

@Component
public class AtLeastOne852RequiredHoldingsValidationRule extends AbstractHoldingsValidationRule {

  private static final String TAG_CODE = "852";

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    List<FieldItem> fieldsWith852TagCode = filterFieldsByTagCode(fieldItems, TAG_CODE);
    return atLeastOneRequiredCondition().apply(TAG_CODE, fieldsWith852TagCode);
  }

}
