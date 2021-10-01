package org.folio.qm.validation;


import static org.folio.qm.converter.elements.LeaderItem.BASE_ADDRESS;
import static org.folio.qm.converter.elements.LeaderItem.CODING_SCHEME;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_20;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_21;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_22;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_23;
import static org.folio.qm.converter.elements.LeaderItem.INDICATOR_COUNT;
import static org.folio.qm.converter.elements.LeaderItem.RECORD_LENGTH;
import static org.folio.qm.converter.elements.LeaderItem.SUBFIELD_CODE_LENGTH;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.folio.qm.converter.elements.Constants;
import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;

public interface LeaderValidationRule {

  List<LeaderItem> COMMON_LEADER_ITEMS = List.of(CODING_SCHEME, INDICATOR_COUNT, SUBFIELD_CODE_LENGTH,
    ENTRY_MAP_20, ENTRY_MAP_21, ENTRY_MAP_22, ENTRY_MAP_23);

  boolean supportFormat(MarcFormat marcFormat);

  Optional<ValidationError> validate(String leader);

  default Optional<ValidationError> commonLeaderValidation(String leader, List<LeaderItem> leaderItems) {
    List<LeaderItem> summaryLeaderFields = new ArrayList<>(COMMON_LEADER_ITEMS);
    summaryLeaderFields.addAll(leaderItems);
    return Stream.of(
        validateLeaderLength(leader),
        validateLeaderNumberFields(leader, RECORD_LENGTH.getPosition(), RECORD_LENGTH.getLength()),
        validateLeaderNumberFields(leader, BASE_ADDRESS.getPosition(), BASE_ADDRESS.getLength()),
        validateLeaderFieldsRestrictions(leader, summaryLeaderFields))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findFirst();
  }

  default Optional<ValidationError> validateLeaderFieldsRestrictions(String leader, List<LeaderItem> leaderFields) {
    Optional<LeaderItem> wrongField = leaderFields.stream()
      .filter(f -> !f.getPossibleValues().contains(leader.charAt(f.getPosition())))
      .findFirst();

    if (wrongField.isPresent()) {
      int position = wrongField.get().getPosition();
      return Optional.of(createValidationError(wrongField.get().getName(),
        String.format("Wrong value '%s', on position %d. Allowed only: %s", leader.charAt(position), position, wrongField.get().getPossibleValues())));
    }
    return Optional.empty();
  }

  private Optional<ValidationError> validateLeaderLength(String leader) {
    return Constants.LEADER_LENGTH == leader.length() ? Optional.empty() : Optional.of(createValidationError(leader, "Wrong leader length"));
  }

  private Optional<ValidationError> validateLeaderNumberFields(String leader, int start, int length) {
    try {
      Integer.parseInt(leader.substring(start, start + length));
      return Optional.empty();
    } catch (NumberFormatException ex) {
      return Optional.of(createValidationError(leader, String.format("%d-%d positions must be a number", start, start + length)));
    }
  }

  private ValidationError createValidationError(String fieldName, String message) {
    return new ValidationError(fieldName, message);
  }
}
