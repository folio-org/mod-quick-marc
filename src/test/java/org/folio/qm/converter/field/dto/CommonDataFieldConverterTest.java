package org.folio.qm.converter.field.dto;

import static org.folio.qm.domain.dto.MarcFormat.AUTHORITY;
import static org.folio.qm.domain.dto.MarcFormat.HOLDINGS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.DataField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;

@UnitTest
class CommonDataFieldConverterTest {

  private static final MarcFormat[] SUPPORTED_FORMATS = {HOLDINGS, AUTHORITY};
  private final CommonDataFieldConverter converter = new CommonDataFieldConverter();

  private static Stream<Arguments> fieldData() {
    return Stream.of(
      arguments(new DataFieldImpl("948", '1', '2'),
        new String[] {"a", "1", "b", "a", "d", "b", "e", "2", "1", "3"},
        new FieldItem().tag("948").indicators(List.of("1", "2")).content("$a 1 $b a $d b $e 2 $1 3")
      ),
      arguments(new DataFieldImpl("010", '1', '1'),
        new String[] {"a", " 2001000234"},
        new FieldItem().tag("010").indicators(List.of("1", "1")).content("$a  2001000234")
      ),
      arguments(new DataFieldImpl("010", '1', '1'),
        new String[] {"a", "sn2003045678 "},
        new FieldItem().tag("010").indicators(List.of("1", "1")).content("$a sn2003045678 ")
      ),
      arguments(new DataFieldImpl("010", '1', '1'),
        new String[] {"a", "34005678 "},
        new FieldItem().tag("010").indicators(List.of("1", "1")).content("$a 34005678 ")
      ),
      arguments(new DataFieldImpl("010", ' ', ' '),
        new String[] {"a", "  34005678 /M"},
        new FieldItem().tag("010").indicators(List.of("\\", "\\")).content("$a   34005678 /M")
      ),
      arguments(new DataFieldImpl("010", ' ', ' '),
        new String[] {"a", "34005678", "9", "2c4750ae-fb1f-4f6f-8ef9-9ccd9ff1bf3b"},
        new FieldItem().tag("010")
          .indicators(List.of("\\", "\\"))
          .content("$a 34005678 $9 2c4750ae-fb1f-4f6f-8ef9-9ccd9ff1bf3b")
      ),
      arguments(new DataFieldImpl("014", '0', ' '),
        new String[] {"9", "not-valid-authority-uiid"},
        new FieldItem().tag("014")
          .indicators(List.of("0", "\\"))
          .content("$9 not-valid-authority-uiid")
      )
    );
  }

  public static Stream<Arguments> dataFields() {
    return IntStream.range(10, 999)
      .mapToObj(value -> String.format("%03d", value))
      .map(tag -> new DataFieldImpl(tag, '0', '0'))
      .map(Arguments::arguments);
  }

  @ParameterizedTest
  @MethodSource("fieldData")
  void testConvertField(DataField dtoField, String[] dtoContent, FieldItem expectedQmField) {
    for (int i = 0; i < dtoContent.length; i += 2) {
      dtoField.addSubfield(new SubfieldImpl(dtoContent[i].charAt(0), dtoContent[i + 1]));
    }
    var actualQmField = converter.convert(dtoField, null);
    assertEquals(expectedQmField.toString(), actualQmField.toString());
  }

  @ParameterizedTest
  @MethodSource("dataFields")
  void testCanProcessField(DataField dtoField) {
    assertTrue(converter.canProcess(dtoField, SUPPORTED_FORMATS[(int) Math.round(Math.random())]));
  }

  @ParameterizedTest
  @MethodSource("dataFields")
  void testCannotProcessField(DataField dtoField) {
    assertFalse(converter.canProcess(dtoField, MarcFormat.BIBLIOGRAPHIC));
  }
}
