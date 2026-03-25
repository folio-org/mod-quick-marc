package org.folio.qm.validation.leader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.validation.ValidationError;
import org.folio.qm.service.validation.impl.authority.AuthorityLeaderValidationRule;
import org.folio.spring.testing.type.UnitTest;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

@UnitTest
class AuthorityLeaderValidationRuleTest {

  private static final String VALID_LEADER = "00241xz\\\\a2200109nu\\4500";
  private static final String WRONG_RECORD_STATUS = "00241zz\\\\a2200109nu\\4500";
  private static final String WRONG_RECORD_TYPE = "00241xx\\\\a2200109nu\\4500";
  private static final String WRONG_UNDEFINED_CHARACTER = "00241xzz\\a2200109nu\\4500";
  private static final String WRONG_ENCODING_LEVEL = "00241xz\\\\a2200109au\\4500";
  private static final String WRONG_PUNCTUATION_POLICY = "00241xz\\\\a2200109nz\\4500";

  private final AuthorityLeaderValidationRule rule = new AuthorityLeaderValidationRule();

  @Test
  void shouldValidateAuthorityLeaderWithOutErrors() {
    var validationError = rule.validate(VALID_LEADER);
    assertTrue(validationError.isEmpty());
  }

  @Test
  void shouldSupportAuthorityFormat() {
    assertTrue(rule.supportFormat(MarcFormat.AUTHORITY));
  }

  @Test
  void shouldValidateAuthorityLeaderWithErrorOnRecordStatus() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RECORD_STATUS);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().tag(), Is.is("Authority record status"));
  }

  @Test
  void shouldValidateAuthorityLeaderWithErrorOnRecordType() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RECORD_TYPE);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().tag(), Is.is("Authority type of record"));
  }

  @Test
  void shouldValidateAuthorityLeaderWithErrorOnUndefinedCharacter() {
    Optional<ValidationError> validationError = rule.validate(WRONG_UNDEFINED_CHARACTER);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().tag(), Is.is("Undefined character position"));
  }

  @Test
  void shouldValidateAuthorityLeaderWithErrorOnEncodingLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_ENCODING_LEVEL);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().tag(), Is.is("Authority encoding level"));
  }

  @Test
  void shouldValidateAuthorityLeaderWithErrorOnPunctuationPolicy() {
    Optional<ValidationError> validationError = rule.validate(WRONG_PUNCTUATION_POLICY);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().tag(), Is.is("Punctuation policy"));
  }
}
