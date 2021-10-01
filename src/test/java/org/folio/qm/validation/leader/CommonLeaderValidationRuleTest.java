package org.folio.qm.validation.leader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import org.folio.qm.validation.LeaderValidationRule;
import org.folio.qm.validation.ValidationError;
import org.folio.qm.validation.impl.holdings.HoldingsLeaderValidationRule;

class CommonLeaderValidationRuleTest {
  private LeaderValidationRule rule = new HoldingsLeaderValidationRule();
  private static final String WRONG_RECORD_LENGTH = "00241cx\\\\a2200109zn\\45000";
  private static final String LEADER_RECORD_LENGTH_WITH_CHARS = "0a241cx\\\\a2200109zn\\4500";
  private static final String WRONG_CODING_SCHEME = "00241cx\\\\b2200109zn\\4500";
  private static final String WRONG_INDICATOR_COUNT = "00241cx\\\\a0200109zn\\4500";
  private static final String WRONG_SUBFIELD_CODE_LENGTH = "00241cx\\\\a2000109zn\\4500";
  private static final String WRONG_ENTRY_MAP_20 = "00241cx\\\\a2200109zn\\5500";
  private static final String WRONG_ENTRY_MAP_21 = "00241cx\\\\a2200109zn\\4400";
  private static final String WRONG_ENTRY_MAP_22 = "00241cx\\\\a2200109zn\\4510";
  private static final String WRONG_ENTRY_MAP_23 = "00241cx\\\\a2200109zn\\4501";

  @Test
  void shouldValidateLeaderWithWrongLengthError() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RECORD_LENGTH);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getMessage(), Is.is("Wrong leader length"));
  }

  @Test
  void shouldValidateNullLeaderWithWrongLengthError() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RECORD_LENGTH);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getMessage(), Is.is("Wrong leader length"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnRecordLength() {
    Optional<ValidationError> validationError = rule.validate(LEADER_RECORD_LENGTH_WITH_CHARS);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getMessage(), Is.is("0-5 positions must be a number"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnCodingScheme() {
    Optional<ValidationError> validationError = rule.validate(WRONG_CODING_SCHEME);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Character coding scheme"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnIndicatorCount() {
    Optional<ValidationError> validationError = rule.validate(WRONG_INDICATOR_COUNT);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Indicator count"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnSubfieldCodeLength() {
    Optional<ValidationError> validationError = rule.validate(WRONG_SUBFIELD_CODE_LENGTH);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Subfield code length"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnEntryMap20() {
    Optional<ValidationError> validationError = rule.validate(WRONG_ENTRY_MAP_20);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Entry map"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnEntryMap21() {
    Optional<ValidationError> validationError = rule.validate(WRONG_ENTRY_MAP_21);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Entry map"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnEntryMap22() {
    Optional<ValidationError> validationError = rule.validate(WRONG_ENTRY_MAP_22);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Entry map"));
  }

  @Test
  void shouldValidateLeaderWithErrorOnEntryMap23() {
    Optional<ValidationError> validationError = rule.validate(WRONG_ENTRY_MAP_23);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Entry map"));
  }
}
