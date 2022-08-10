package org.folio.qm.validation.impl.authority;

import static org.folio.qm.converter.elements.Constants.TAG_008_AUTHORITY_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.TAG_008_CONTROL_FIELD;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;
import org.springframework.stereotype.Component;

@Component
public class ExactSize008AuthorityFieldValidationRule extends AbstractAuthorityValidationRule {

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith008TagCode = filterFieldsByTagCode(fieldItems, TAG_008_CONTROL_FIELD);
    return contentLengthCondition()
      .apply(Pair.of(TAG_008_CONTROL_FIELD, TAG_008_AUTHORITY_CONTROL_FIELD_LENGTH), fieldsWith008TagCode);
  }
}
