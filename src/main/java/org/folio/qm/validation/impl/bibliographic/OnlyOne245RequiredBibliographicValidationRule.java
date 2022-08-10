package org.folio.qm.validation.impl.bibliographic;

import java.util.List;
import java.util.Optional;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;
import org.springframework.stereotype.Component;

@Component
public class OnlyOne245RequiredBibliographicValidationRule extends AbstractBibliographicValidationRule {

  private static final String TAG_CODE = "245";

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith004TagCode = filterFieldsByTagCode(fieldItems, TAG_CODE);
    return onlyOneRequiredCondition().apply(TAG_CODE, fieldsWith004TagCode);
  }
}
