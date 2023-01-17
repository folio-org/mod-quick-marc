package org.folio.qm.validation.impl.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.validation.FieldValidationRule.EMPTY_CONTENT_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class OnlyOne010AllowedAuthorityValidationRuleTest {

  private final OnlyOne010AllowedAuthorityValidationRule rule = new OnlyOne010AllowedAuthorityValidationRule();

  @Test
  void testValidationRulePassedWhenOnlyOne010TagExists() {
    var fields = List.of(new FieldItem().tag("010").content("test content"));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isEmpty();
  }

  @Test
  void testValidationRuleFailedWhenSeveral010TagExist() {
    var fields = List.of(
      new FieldItem().tag("010").content("test content"),
      new FieldItem().tag("010").content("test content 2")
    );
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(IS_UNIQUE_TAG_ERROR_MSG, validationError.getMessage()));
  }

  @Test
  void testValidationRulePassedWhen010TagNotExist() {
    var fields = List.of(new FieldItem().tag("200").content("test content"));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isEmpty();
  }

  @Test
  void testValidationRuleFailedWhen010TagHasEmptyContent() {
    var fields = List.of(new FieldItem().tag("010").content(""));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isPresent()
      .hasValueSatisfying(validationError -> assertEquals(EMPTY_CONTENT_ERROR_MSG, validationError.getMessage()));
  }
}
