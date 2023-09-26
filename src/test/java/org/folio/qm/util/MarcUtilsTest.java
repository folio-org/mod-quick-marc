package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.UUID;
import java.util.stream.Stream;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

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

}
