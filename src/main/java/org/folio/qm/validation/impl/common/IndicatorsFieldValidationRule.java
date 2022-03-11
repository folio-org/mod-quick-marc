package org.folio.qm.validation.impl.common;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.FieldValidationRule;
import org.folio.qm.validation.ValidationError;

@Component
public class IndicatorsFieldValidationRule extends FieldValidationRule {

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    for (FieldItem fieldItem : fieldItems) {
      var indicators = fieldItem.getIndicators();
      if (indicators != null && !indicators.isEmpty()) {
        if (indicators.size() != 2) {
          return Optional.of(createValidationError(fieldItem.getTag(), "Should have exactly 2 indicators"));
        } else if (isValidIndicator(indicators.get(0)) || isValidIndicator(indicators.get(1))) {
          return Optional.of(createValidationError(fieldItem.getTag(), "Indicator could have only one-character value"));
        }
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return true;
  }

  private boolean isValidIndicator(String indicator) {
    return indicator.length() != 1;
  }
}
