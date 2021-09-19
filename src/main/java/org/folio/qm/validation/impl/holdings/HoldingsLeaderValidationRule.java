package org.folio.qm.validation.impl.holdings;

import static org.folio.qm.converter.elements.LeaderItem.HOLDINGS_ENCODING_LEVEL;
import static org.folio.qm.converter.elements.LeaderItem.HOLDINGS_RECORD_STATUS;
import static org.folio.qm.converter.elements.LeaderItem.HOLDINGS_RECORD_TYPE;
import static org.folio.qm.converter.elements.LeaderItem.ITEM_INFORMATION;
import static org.folio.qm.converter.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_19;
import static org.folio.qm.converter.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_7;
import static org.folio.qm.converter.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_8;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.LeaderValidationRule;
import org.folio.qm.validation.ValidationError;

@Component
public class HoldingsLeaderValidationRule implements LeaderValidationRule {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.HOLDINGS == marcFormat;
  }

  @Override
  public Optional<ValidationError> validate(String leader) {
    Optional<ValidationError> error = commonLeaderValidation(leader);
    if (error.isPresent()) {
      return error;
    }
    return validateLeaderFieldsRestrictions(leader, List.of(HOLDINGS_RECORD_STATUS, HOLDINGS_RECORD_TYPE,
      HOLDINGS_ENCODING_LEVEL, ITEM_INFORMATION, UNDEFINED_CHARACTER_POSITION_7, UNDEFINED_CHARACTER_POSITION_8,
      UNDEFINED_CHARACTER_POSITION_19));
  }
}
