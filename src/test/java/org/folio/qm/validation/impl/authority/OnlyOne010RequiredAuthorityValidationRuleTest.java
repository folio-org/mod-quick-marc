package org.folio.qm.validation.impl.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
      .hasValueSatisfying(validationError -> assertEquals("Is unique tag", validationError.getMessage()));
  }

  @Test
  void testValidationRulePassedWhen010TagNotExist() {
    var fields = List.of(new FieldItem().tag("200").content("test content"));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isEmpty();
  }

  @Test
  void testValidationRulePassedWhen010TagHasEmptyContent() {
    var fields = List.of(new FieldItem().tag("010").content(""));
    var validationResult = rule.validate(fields);
    assertThat(validationResult).isEmpty();
  }
}
