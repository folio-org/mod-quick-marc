package org.folio.qm.validation.impl.holdings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.validation.ValidationError;

@Component
public class OnlyOne004ExistHoldingsValidationRule extends AbstractHoldingsValidationRule {

  private static final String TAG_CODE = "004";

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fieldItems) {
    var fieldsWith004TagCode = fieldItems.stream()
      .filter(fieldItem -> TAG_CODE.equals(fieldItem.getTag()))
      .collect(Collectors.toList());
    if (fieldsWith004TagCode.isEmpty()) {
      return Optional.of(createValidationError(TAG_CODE, "Is required tag"));
    } else if (fieldsWith004TagCode.size() != 1) {
      return Optional.of(createValidationError(TAG_CODE, "Is unique tag"));
    } else if (StringUtils.isEmpty((CharSequence) fieldsWith004TagCode.get(0).getContent())) {
      return Optional.of(createValidationError(TAG_CODE, "Content couldn't be empty"));
    }
    return Optional.empty();
  }
}
