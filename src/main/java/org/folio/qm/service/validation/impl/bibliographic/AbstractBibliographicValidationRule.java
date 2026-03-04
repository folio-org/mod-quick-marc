package org.folio.qm.service.validation.impl.bibliographic;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.validation.FieldValidationRule;

public abstract class AbstractBibliographicValidationRule extends FieldValidationRule {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.BIBLIOGRAPHIC == marcFormat;
  }
}
