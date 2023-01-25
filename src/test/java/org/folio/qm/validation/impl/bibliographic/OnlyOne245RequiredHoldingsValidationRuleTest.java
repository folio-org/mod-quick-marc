package org.folio.qm.validation.impl.bibliographic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.validation.FieldValidationRule.EMPTY_CONTENT_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_REQUIRED_TAG_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class OnlyOne245RequiredHoldingsValidationRuleTest {

  private final OnlyOne245RequiredBibliographicValidationRule rule =
    new OnlyOne245RequiredBibliographicValidationRule();

  @Test
  void testValidationRulePassedWhenOnlyOne245TagExists() {
    var fields = List.of(new FieldItem().tag("245").content("test content"));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isEmpty();
  }

  @Test
  void testValidationRuleFailedWhenMultiple245TagExist() {
    var fields = List.of(
      new FieldItem().tag("245").content("test content"),
      new FieldItem().tag("245").content("test content 2")
    );
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(IS_UNIQUE_TAG_ERROR_MSG, validationError.getMessage()));
  }

  @Test
  void testValidationRuleFailedWhen245TagNotExist() {
    var fields = List.of(new FieldItem().tag("200").content("test content"));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isPresent()
      .hasValueSatisfying(validationError -> assertEquals(IS_REQUIRED_TAG_ERROR_MSG, validationError.getMessage()));
  }

  @Test
  void testValidationRuleFailedWhen245TagHasEmptyContent() {
    var fields = List.of(new FieldItem().tag("245").content(""));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isPresent()
      .hasValueSatisfying(validationError -> assertEquals(EMPTY_CONTENT_ERROR_MSG, validationError.getMessage()));
  }
}
