package org.folio.qm.converter.field.qm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.DataField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;

@UnitTest
class CommonFieldItemConverterTest {

  private final CommonFieldItemConverter converter = new CommonFieldItemConverter();

  private static Stream<Arguments> fieldData() {
    return Stream.of(
      arguments(new DataFieldImpl("948", '1', '2'),
        new String[] {"a", "1", "b", "a", "d", "b", "e", "2", "1", "3"},
        new FieldItem().tag("948").indicators(List.of("1", "2")).content("$a 1 $b a $d b $e 2 $1 3")
      ),
      arguments(new DataFieldImpl("011", ' ', '1'),
        new String[] {"a", "2001000234"},
        new FieldItem().tag("011").indicators(List.of("\\", "1")).content("$a2001000234")
      ),
      arguments(new DataFieldImpl("300", '1', ' '),
        new String[] {"a", "2001000234", "c", "123456789"},
        new FieldItem().tag("300").indicators(List.of("1", "\\")).content("$a2001000234$c123456789")
      ),
      arguments(new DataFieldImpl("666", ' ', ' '),
        new String[] {"a", "a  2", "c", "00"},
        new FieldItem().tag("666").indicators(List.of("\\", "\\")).content("$aa  2$c00")
      ),
      arguments(new DataFieldImpl("999", 'f', 'f'),
        new String[] {"s", "a", "i", "b"},
        new FieldItem().tag("999").indicators(List.of("f", "f")).content("$s a $i b")
      )
    );
  }

  public static Stream<Arguments> canProcessFields() {
    return IntStream.range(11, 999)
      .mapToObj(value -> String.format("%03d", value))
      .map(tag -> new FieldItem().tag(tag))
      .map(Arguments::arguments);
  }

  public static Stream<Arguments> cannotProcessFields() {
    return IntStream.range(1, 11)
      .mapToObj(value -> String.format("%03d", value))
      .map(tag -> new FieldItem().tag(tag))
      .map(Arguments::arguments);
  }

  @ParameterizedTest
  @MethodSource("fieldData")
  void testConvertField(DataField expectedDtoField, String[] dtoContent, FieldItem qmField) {
    for (int i = 0; i < dtoContent.length; i += 2) {
      expectedDtoField.addSubfield(new SubfieldImpl(dtoContent[i].charAt(0), dtoContent[i + 1]));
    }
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
