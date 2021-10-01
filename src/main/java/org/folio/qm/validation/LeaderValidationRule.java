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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.folio.qm.converter.elements.Constants;
import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;

public interface LeaderValidationRule {

  boolean supportFormat(MarcFormat marcFormat);

  Optional<ValidationError> validate(String leader);

  default Optional<ValidationError> commonLeaderValidation(String leader) {
    return Stream.of(
        validateLeaderLength(leader),
        validateLeaderNumberFields(leader, RECORD_LENGTH.getPosition(), RECORD_LENGTH.getLength()),
        validateLeaderNumberFields(leader, BASE_ADDRESS.getPosition(), BASE_ADDRESS.getLength()),
        validateLeaderFieldsRestrictions(leader, List.of(CODING_SCHEME, INDICATOR_COUNT, SUBFIELD_CODE_LENGTH,
          ENTRY_MAP_20, ENTRY_MAP_21, ENTRY_MAP_22, ENTRY_MAP_23)))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findFirst();
  }

  default Optional<ValidationError> validateLeaderFieldsRestrictions(String leader, List<LeaderItem> leaderItems) {
    return leaderItems.stream()
      .filter(item -> !isValidLeaderValue(leader, item))
      .findFirst()
      .map(invalidItem -> {
        var position = invalidItem.getPosition();
        var message = String.format("Wrong value '%s', on position %d. Allowed only: %s",
          leader.charAt(position), position, invalidItem.getPossibleValues());
        return createValidationError(invalidItem.getName(), message);
      });
  }

  private Optional<ValidationError> validateLeaderLength(String leader) {
    return Constants.LEADER_LENGTH == leader.length()
      ? Optional.empty()
      : Optional.of(createValidationError(leader, "Wrong leader length"));
  }

  private Optional<ValidationError> validateLeaderNumberFields(String leader, int start, int length) {
    try {
      Integer.parseInt(leader.substring(start, start + length));
      return Optional.empty();
    } catch (NumberFormatException ex) {
      var message = String.format("%d-%d positions must be a number", start, start + length);
      return Optional.of(createValidationError(leader, message));
    }
  }

  private boolean isValidLeaderValue(String leader, LeaderItem item) {
    return item.getPossibleValues().contains(leader.charAt(item.getPosition()))
      || item.getPossibleValues().contains(Character.toLowerCase(leader.charAt(item.getPosition())));
  }

  private ValidationError createValidationError(String fieldName, String message) {
    return new ValidationError(fieldName, message);
  }
}
