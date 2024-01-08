package org.folio.qm.validation.impl.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.validation.FieldValidationRule.EMPTY_CONTENT_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_REQUIRED_TAG_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class OnlyOne1XxRequiredAuthorityValidationRuleTest {

  private final OnlyOne1xxRequiredAuthorityValidationRule rule = new OnlyOne1xxRequiredAuthorityValidationRule();

  private static Stream<Arguments> provide1xxTagArguments() {
    return IntStream.range(100, 199).boxed()
      .map(integer -> Arguments.of(String.valueOf(integer)));
  }

  @ParameterizedTest
  @MethodSource("provide1xxTagArguments")
  void testValidationRuleSucceedWhenOnlyOne1xxTagExists(String tag) {
    var fields = List.of(new FieldItem().tag(tag).content("test content"));
    var validationResult = rule.validate(fields);
    assertTrue(validationResult.isEmpty());
  }

  @Test
  void testValidationRuleFailedWhenOnlyOne1xxTagExistsButContentIsEmpty() {
    var fields = List.of(new FieldItem().tag("100").content(""));
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(EMPTY_CONTENT_ERROR_MSG, validationError.message()));
  }

  @Test
  void testValidationRuleFailedWhenSeveral1xxTagExist() {
    var fields = List.of(
      new FieldItem().tag("100").content("test content"),
      new FieldItem().tag("101").content("test content 2")
    );
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(IS_UNIQUE_TAG_ERROR_MSG, validationError.message()));
  }

  @Test
  void testValidationRuleFailedWhen1xxTagNotExist() {
    var fields = List.of(new FieldItem().tag("200").content("test content"));
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(IS_REQUIRED_TAG_ERROR_MSG, validationError.message()));
  }

}
