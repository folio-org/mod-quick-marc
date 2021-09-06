package org.folio.qm.validation.impl.bibliographic;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.FieldValidationRule;

public abstract class AbstractBibliographicValidationRule implements FieldValidationRule {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.BIBLIOGRAPHIC == marcFormat;
  }
}
