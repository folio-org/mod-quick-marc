package org.folio.qm.validation.impl.holdings;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;

@Component
public class AtLeastOne852ExistHoldingsValidationRule extends AbstractHoldingsValidationRule {

  private static final String TAG_CODE = "852";

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    List<FieldItem> fieldsWith852TagCode = filterFieldsByTagCode(fieldItems, TAG_CODE);
    if (fieldsWith852TagCode.isEmpty()) {
      return Optional.of(createValidationError(TAG_CODE, "Is required tag"));
    } else {
      for (FieldItem fieldItem : fieldsWith852TagCode) {
        if (StringUtils.isEmpty((CharSequence) fieldItem.getContent())) {
          return Optional.of(createValidationError(TAG_CODE, "Content couldn't be empty"));
        }
      }
    }
    return Optional.empty();
  }

}
