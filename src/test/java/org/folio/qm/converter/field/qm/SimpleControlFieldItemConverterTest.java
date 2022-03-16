package org.folio.qm.converter.field.qm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.impl.ControlFieldImpl;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.support.types.UnitTest;

@UnitTest
class SimpleControlFieldItemConverterTest {

  private final SimpleControlFieldItemConverter converter = new SimpleControlFieldItemConverter();

  private static Stream<Arguments> fieldData() {
    return Stream.of(
      arguments(new ControlFieldImpl("001", "abcdefg"),
        new FieldItem().tag("001").content("abcdefg")
      ),
      arguments(new ControlFieldImpl("002", "abcdefg"),
        new FieldItem().tag("002").indicators(Collections.emptyList()).content("abcdefg")
      ),
      arguments(new ControlFieldImpl("003", "abcdefg"),
        new FieldItem().tag("003").indicators(null).content("abcdefg")
      ),
      arguments(new ControlFieldImpl("004", "abcdefg"),
        new FieldItem().tag("004").indicators(List.of("")).content("abcdefg")
      ),
      arguments(new ControlFieldImpl("005", "abcdefg"),
        new FieldItem().tag("005").indicators(List.of("1", "2")).content("abcdefg")
      ),
      arguments(new ControlFieldImpl("009", "abcdefg"),
        new FieldItem().tag("009").indicators(List.of("1", "2", "3")).content("abcdefg")
      )
    );
  }

  public static Stream<Arguments> canProcessFields() {
    return IntStream.of(1, 2,3,4,5,9)
      .mapToObj(value -> String.format("%03d", value))
      .map(tag -> new FieldItem().tag(tag))
      .map(Arguments::arguments);
  }

  public static Stream<Arguments> cannotProcessFields() {
    return IntStream.of(6, 7, 8, 10, 11, 500)
      .mapToObj(value -> String.format("%03d", value))
      .map(tag -> new FieldItem().tag(tag))
      .map(Arguments::arguments);
  }

  @ParameterizedTest
  @MethodSource("fieldData")
  void testConvertField(ControlField expectedDtoField, FieldItem qmField) {
    var actualDtoField = converter.convert(qmField);
    assertEquals(expectedDtoField.toString(), actualDtoField.toString());
  }

  @ParameterizedTest
  @MethodSource("canProcessFields")
  void testCanProcessField(FieldItem fieldItem) {
    assertTrue(converter.canProcess(fieldItem, null));
  }

  @ParameterizedTest
  @MethodSource("cannotProcessFields")
  void testCannotProcessField(FieldItem fieldItem) {
    assertFalse(converter.canProcess(fieldItem, null));
  }
}
