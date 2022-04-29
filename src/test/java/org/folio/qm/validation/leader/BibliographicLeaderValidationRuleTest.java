package org.folio.qm.validation.leader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.validation.ValidationError;
import org.folio.qm.validation.impl.bibliographic.BibliographicLeaderValidationRule;

@UnitTest
class BibliographicLeaderValidationRuleTest {

  private static final String VALID_LEADER = "01706ccm\\a2200361\\\\\\4500";
  private static final String WRONG_BIB_RECORD_STATUS = "01706xcm\\a2200361\\\\\\4500";
  private static final String WRONG_BIB_RECORD_TYPE = "01706cxm\\a2200361\\\\\\4500";
  private static final String WRONG_BIBLIOGRAPHIC_LEVEL = "01706ccx\\a2200361\\\\\\4500";
  private static final String WRONG_CONTROL_TYPE = "01706ccmxa2200361\\\\\\4500";
  private static final String WRONG_BIB_BIB_ENCODING_LEVEL = "01706ccm\\a2200361x\\\\4500";
  private static final String WRONG_CATALOGING_FORM = "01706ccm\\a2200361\\x\\4500";
  private static final String WRONG_RESOURCE_RECORD_LEVEL = "01706ccm\\a2200361\\\\x4500";

  private final BibliographicLeaderValidationRule rule = new BibliographicLeaderValidationRule();

  @Test
  void shouldValidateBibliographicLeaderWithoutErrors() {
    var validationError = rule.validate(VALID_LEADER);
    assertTrue(validationError.isEmpty());
  }

  @Test
  void shouldSupportBibliographicFormat() {
    assertTrue(rule.supportFormat(MarcFormat.BIBLIOGRAPHIC));
  }

  @Test
  void shouldValidateBibliographicLeaderWithErrorOnRecordStatus() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIB_RECORD_STATUS);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Bib record status"));
  }

  @Test
  void shouldValidateBibliographicLeaderWithErrorOnRecordType() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIB_RECORD_TYPE);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Bib type of record"));
  }

  @Test
  void shouldValidateBibliographicLeaderWithErrorOnBibliographicLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIBLIOGRAPHIC_LEVEL);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Bibliographic level"));
  }

  @Test
  void shouldValidateBibliographicLeaderWithErrorOnControlType() {
    Optional<ValidationError> validationError = rule.validate(WRONG_CONTROL_TYPE);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Type of control"));
  }

  @Test
  void shouldValidateBibliographicLeaderWithoutErrorOnEncodingLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_BIB_BIB_ENCODING_LEVEL);
    assertTrue(validationError.isEmpty());
  }

  @Test
  void shouldValidateBibliographicLeaderWithErrorOnCatalogingForm() {
    Optional<ValidationError> validationError = rule.validate(WRONG_CATALOGING_FORM);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Descriptive cataloging form"));
  }

  @Test
  void shouldValidateBibliographicLeaderWithErrorOnResourceRecordLevel() {
    Optional<ValidationError> validationError = rule.validate(WRONG_RESOURCE_RECORD_LEVEL);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("Multipart resource record level"));
  }
}
