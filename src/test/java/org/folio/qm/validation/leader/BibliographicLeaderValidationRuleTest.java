package org.folio.qm.validation.leader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import org.folio.qm.validation.LeaderValidationRule;
import org.folio.qm.validation.ValidationError;
import org.folio.qm.validation.impl.bibliographic.BibliographicLeaderValidationRule;

class BibliographicLeaderValidationRuleTest {

  private LeaderValidationRule rule = new BibliographicLeaderValidationRule();
  public static final String VALID_LEADER = "01706ccm\\a2200361\\\\\\4500";
  public static final String WRONG_BIB_RECORD_STATUS = "01706xcm\\a2200361\\\\\\4500";
  public static final String WRONG_BIB_RECORD_TYPE = "01706cxm\\a2200361\\\\\\4500";
  public static final String WRONG_BIBLIOGRAPHIC_LEVEL = "01706ccx\\a2200361\\\\\\4500";
  public static final String WRONG_CONTROL_TYPE = "01706ccmxa2200361\\\\\\4500";
  public static final String WRONG_BIB_BIB_ENCODING_LEVEL = "01706ccm\\a2200361x\\\\4500";
  public static final String WRONG_CATALOGING_FORM = "01706ccm\\a2200361\\x\\4500";
  public static final String WRONG_RESOURCE_RECORD_LEVEL = "01706ccm\\a2200361\\\\x4500";



  @Test
  void shouldValidateHoldingsLeaderWithOutErrors() {
    assertDoesNotThrow(() -> rule.validate(VALID_LEADER));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnRecordStatus() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIB_RECORD_STATUS);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Bib record status"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnRecordType() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIB_RECORD_TYPE);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Bib type of record"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnBibliographicLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIBLIOGRAPHIC_LEVEL);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Bibliographic level"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnControlType() {
    Optional<ValidationError> validationError = rule.validate(WRONG_CONTROL_TYPE);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Type of control"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnEncodingLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIB_BIB_ENCODING_LEVEL);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Bib encoding level"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnCatalogingForm() {
    Optional<ValidationError> validationError = rule.validate(WRONG_CATALOGING_FORM);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Descriptive cataloging form"));
  }

  @Test
  void shouldValidateHoldingsLeaderWithErrorOnResourceRecordLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RESOURCE_RECORD_LEVEL);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Multipart resource record level"));
  }
}
