package org.folio.qm.service.validation.impl.authority;

import static org.folio.qm.converter.elements.LeaderItem.AUTHORITY_ENCODING_LEVEL;
import static org.folio.qm.converter.elements.LeaderItem.AUTHORITY_RECORD_STATUS;
import static org.folio.qm.converter.elements.LeaderItem.AUTHORITY_RECORD_TYPE;
import static org.folio.qm.converter.elements.LeaderItem.PUNCTUATION_POLICY;
import static org.folio.qm.converter.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_19;
import static org.folio.qm.converter.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_7;
import static org.folio.qm.converter.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_8;

import java.util.List;
import java.util.Optional;
import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.validation.LeaderValidationRule;
import org.folio.qm.service.validation.ValidationError;
import org.springframework.stereotype.Component;

@Component
public class AuthorityLeaderValidationRule extends LeaderValidationRule {

  private static final List<LeaderItem> AUTHORITY_LEADER_ITEMS = List.of(AUTHORITY_RECORD_STATUS, AUTHORITY_RECORD_TYPE,
    AUTHORITY_ENCODING_LEVEL, PUNCTUATION_POLICY, UNDEFINED_CHARACTER_POSITION_7, UNDEFINED_CHARACTER_POSITION_8,
    UNDEFINED_CHARACTER_POSITION_19);

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.AUTHORITY == marcFormat;
  }

  @Override
  public Optional<ValidationError> validate(String leader) {
    return commonLeaderValidation(leader, AUTHORITY_LEADER_ITEMS);
  }
}
