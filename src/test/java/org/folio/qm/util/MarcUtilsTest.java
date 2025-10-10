package org.folio.qm.util;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.UUID;
import java.util.stream.Stream;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;

@UnitTest
class MarcUtilsTest {

  private static Stream<Arguments> normalizeFixedLengthString_positive_source() {
    return Stream.of(
      arguments(null, "\\\\\\\\\\"),
      arguments("", "\\\\\\\\\\"),
      arguments("hello", "hello"),
      arguments(" hello  ", "hello"),
      arguments("this is a very long string", "this\\")
    );
  }

  @Test
  void whenUuidIsValid_thenValidationSucceeds() {
    assertTrue(MarcUtils.isValidUuid(UUID.randomUUID().toString()));
  }

  @Test
  void whenUuidIsInvalid_thenValidationFails() {
    assertFalse(MarcUtils.isValidUuid("invalid-uuid"));
  }

  @ParameterizedTest
  @MethodSource("normalizeFixedLengthString_positive_source")
  void normalizeFixedLengthString_positive(String sourceString, String expected) {
    int length = 5;

    String actual = MarcUtils.normalizeFixedLengthString(sourceString, length);

    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -5, Integer.MAX_VALUE})
  void normalizeFixedLengthString_negative_unexpectedLength(int length) {
    String sourceString = "hello";

    assertThrows(IllegalArgumentException.class,
      () -> MarcUtils.normalizeFixedLengthString(sourceString, length));
  }

  @Test
  void extractSubfields_emptyFieldContent() {
    var field = new FieldItem().content(EMPTY);

    var subfields = MarcUtils.extractSubfields(field, s -> new SubfieldImpl());

    assertThat(subfields).isEmpty();
  }

  @Test
  void extractSubfields_emptyLastSubfieldContent() {
    var field = new FieldItem().content("$Aa$bb$c");

    var subfields = MarcUtils.extractSubfields(field, s -> new SubfieldImpl('x', s));

    assertThat(subfields).hasSize(2)
      .extracting(Subfield::getData)
      .containsExactly("$Aa", "$bb");
  }

  @Test
  void extractSubfields_emptyMiddleSubfieldContent() {
    var field = new FieldItem().content("$Aa$b$cc");

    var subfields = MarcUtils.extractSubfields(field, s -> new SubfieldImpl('x', s));

    assertThat(subfields).hasSize(2)
      .extracting(Subfield::getData)
      .containsExactly("$Aa", "$cc");
  }

  @Test
  void extractSubfields_blankMiddleSubfieldContent() {
    var field = new FieldItem().content("$Aa$b $cc");

    var subfields = MarcUtils.extractSubfields(field, s -> new SubfieldImpl('x', s));

    assertThat(subfields).hasSize(2)
      .extracting(Subfield::getData)
      .containsExactly("$Aa", "$cc");
  }

  @Test
  void extractSubfields_emptyFirstSubfieldContent() {
    var field = new FieldItem().content("$A$bb$cc");

    var subfields = MarcUtils.extractSubfields(field, s -> new SubfieldImpl('x', s));

    assertThat(subfields).hasSize(2)
      .extracting(Subfield::getData)
      .containsExactly("$bb", "$cc");
  }
}
