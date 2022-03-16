package org.folio.qm.validation.impl.authority;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.FieldValidationRule;

public abstract class AbstractAuthorityValidationRule extends FieldValidationRule {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.AUTHORITY == marcFormat;
  }
}
