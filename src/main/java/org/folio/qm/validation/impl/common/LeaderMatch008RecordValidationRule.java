package org.folio.qm.validation.impl.common;

import static java.util.Objects.nonNull;

import static org.folio.qm.converter.elements.Constants.DESC;
import static org.folio.qm.converter.elements.Constants.DESC_LEADER_POS;
import static org.folio.qm.converter.elements.Constants.TAG_008_CONTROL_FIELD;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.RecordValidationRule;
import org.folio.qm.validation.ValidationError;

@Component
public class LeaderMatch008RecordValidationRule extends RecordValidationRule {

  private static final String VALIDATION_MESSAGE = "The Leader and 008 do not match";

  @Override
  public Optional<ValidationError> validate(List<FieldItem> fields, String leader) {
    var fieldWith008Tag = filterFieldsByTagCode(fields, TAG_008_CONTROL_FIELD);
    if (!fieldWith008Tag.isEmpty()) {
      for (FieldItem fieldItem : fieldWith008Tag) {
        if (!isLeaderMatches(fieldItem.getContent(), leader)) {
          var validationError = createValidationError(TAG_008_CONTROL_FIELD, VALIDATION_MESSAGE);
          return Optional.ofNullable(validationError);
        }
      }
    }
    return Optional.empty();
  }

  private boolean isLeaderMatches(Object content, String leader) {
    @SuppressWarnings("unchecked")
    var contentMap = ((Map<String, Object>) content);
    return nonNull(contentMap)
      && nonNull(contentMap.get(DESC))
      && contentMap.get(DESC).toString().equals(Character.toString(leader.charAt(DESC_LEADER_POS)));
  }

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat == MarcFormat.BIBLIOGRAPHIC;
  }

}
