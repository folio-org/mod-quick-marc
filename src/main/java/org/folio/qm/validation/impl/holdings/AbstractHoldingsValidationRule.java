package org.folio.qm.validation.impl.holdings;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.FieldValidationRule;

public abstract class AbstractHoldingsValidationRule implements FieldValidationRule {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.HOLDINGS == marcFormat;
  }
}
