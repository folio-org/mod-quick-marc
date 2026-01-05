package org.folio.qm.service.validation.impl.holdings;

import static org.folio.qm.convertion.elements.LeaderItem.HOLDINGS_ENCODING_LEVEL;
import static org.folio.qm.convertion.elements.LeaderItem.HOLDINGS_RECORD_STATUS;
import static org.folio.qm.convertion.elements.LeaderItem.HOLDINGS_RECORD_TYPE;
import static org.folio.qm.convertion.elements.LeaderItem.ITEM_INFORMATION;
import static org.folio.qm.convertion.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_19;
import static org.folio.qm.convertion.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_7;
import static org.folio.qm.convertion.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_8;

import java.util.List;
import java.util.Optional;
import org.folio.qm.convertion.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.validation.LeaderValidationRule;
import org.folio.qm.service.validation.ValidationError;
import org.springframework.stereotype.Component;

@Component
public class HoldingsLeaderValidationRule extends LeaderValidationRule {

  private static final List<LeaderItem> HOLDINGS_LEADER_ITEMS = List.of(HOLDINGS_RECORD_STATUS, HOLDINGS_RECORD_TYPE,
    HOLDINGS_ENCODING_LEVEL, ITEM_INFORMATION, UNDEFINED_CHARACTER_POSITION_7, UNDEFINED_CHARACTER_POSITION_8,
    UNDEFINED_CHARACTER_POSITION_19);

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.HOLDINGS == marcFormat;
  }

  @Override
  public Optional<ValidationError> validate(String leader) {
    return commonLeaderValidation(leader, HOLDINGS_LEADER_ITEMS);
  }
}
