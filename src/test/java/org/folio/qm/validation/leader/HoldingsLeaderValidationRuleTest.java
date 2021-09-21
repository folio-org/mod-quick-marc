package org.folio.qm.validation.leader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import org.folio.qm.validation.LeaderValidationRule;
import org.folio.qm.validation.ValidationError;
import org.folio.qm.validation.impl.holdings.HoldingsLeaderValidationRule;

class HoldingsLeaderValidationRuleTest {

  private LeaderValidationRule rule = new HoldingsLeaderValidationRule();
  public static final String VALID_LEADER = "00241cx\\\\a2200109zn\\4500";
  public static final String WRONG_RECORD_STATUS = "00241ex\\\\a2200109zn\\4500";
  public static final String WRONG_RECORD_TYPE = "00241ca\\\\a2200109zn\\4500";
  public static final String WRONG_UNDEFINED_CHARACTER = "00241cx0\\a2200109zn\\4500";
  public static final String WRONG_ENCODING_LEVEL = "00241cx\\\\a2200109an\\4500";
  public static final String WRONG_ITEM_INFORMATION = "00241cx\\\\a2200109za\\4500";


  @Test
  void shouldValidateHoldingsLeaderWithOutErrors() {
    assertDoesNotThrow(() -> rule.validate(VALID_LEADER));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnRecordLength() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RECORD_STATUS);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Holdings record status"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnRecordType() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RECORD_TYPE);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Holdings type of record"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnUndefinedCharacter() {
    Optional<ValidationError> validationError = rule.validate(WRONG_UNDEFINED_CHARACTER);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Undefined character position"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnEncodingLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_ENCODING_LEVEL);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Holdings encoding level"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnItemInformation() {
    Optional<ValidationError> validationError = rule.validate(WRONG_ITEM_INFORMATION);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Item information"));
  }
}
