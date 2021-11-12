package org.folio.qm.validation.impl.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.folio.qm.domain.dto.FieldItem;

class OnlyOne1XXRequiredAuthorityValidationRuleTest {

  private final OnlyOne1XXRequiredAuthorityValidationRule rule = new OnlyOne1XXRequiredAuthorityValidationRule();

  private static Stream<Arguments> provide1xxTagArguments() {
    return IntStream.range(100, 199).boxed()
      .map(integer -> Arguments.of(String.valueOf(integer)));
  }

  @ParameterizedTest
  @MethodSource("provide1xxTagArguments")
  void testValidationRuleSucceedWhenOnlyOne1XXTagExists(String tag) {
    var fields = List.of(new FieldItem().tag(tag).content("test content"));
    var validationResult = rule.validate(fields);
    assertTrue(validationResult.isEmpty());
  }

  @Test
  void testValidationRuleFailedWhenOnlyOne1XXTagExistsButContentIsEmpty() {
    var fields = List.of(new FieldItem().tag("100").content(""));
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(validationError.getMessage(), "Content couldn't be empty"));
  }

  @Test
  void testValidationRuleFailedWhenSeveral1XXTagExist() {
    var fields = List.of(
      new FieldItem().tag("100").content("test content"),
      new FieldItem().tag("101").content("test content 2")
    );
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(validationError.getMessage(), "Is unique tag"));
  }

  @Test
  void testValidationRuleFailedWhen1XXTagNotExist() {
    var fields = List.of(new FieldItem().tag("200").content("test content"));
    var validationResult = rule.validate(fields);
    assertThat(validationResult)
      .isPresent()
      .hasValueSatisfying(validationError -> assertEquals(validationError.getMessage(), "Is required tag"));
  }

}