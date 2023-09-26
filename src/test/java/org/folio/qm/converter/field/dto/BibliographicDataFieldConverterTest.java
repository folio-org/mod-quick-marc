package org.folio.qm.converter.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.DataField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;

@UnitTest
class BibliographicDataFieldConverterTest {

  private final BibliographicDataFieldConverter converter = new BibliographicDataFieldConverter();

  private static Stream<Arguments> cannotProcessFields() {
    return Stream.of(
      arguments(new DataFieldImpl("014", ' ', ' '), MarcFormat.AUTHORITY),
      arguments(new DataFieldImpl("014", ' ', ' '), MarcFormat.HOLDINGS)
    );
  }

  public static Stream<Arguments> dataFields() {
    return IntStream.range(10, 999)
      .mapToObj(value -> String.format("%03d", value))
      .map(tag -> new DataFieldImpl(tag, '0', '0'))
      .map(Arguments::arguments);
  }

  private static Stream<Arguments> fieldData() {
    return Stream.of(
      arguments(new DataFieldImpl("010", ' ', ' '),
        new String[] {"a", "34005678", "9", "2c4750ae-fb1f-4f6f-8ef9-9ccd9ff1bf3b"},
        new FieldItem().tag("010")
          .indicators(List.of("\\", "\\"))
          .content("$a 34005678 $9 2c4750ae-fb1f-4f6f-8ef9-9ccd9ff1bf3b")
          .linkDetails(new LinkDetails().authorityId(UUID.fromString("2c4750ae-fb1f-4f6f-8ef9-9ccd9ff1bf3b")))
      ),
      arguments(new DataFieldImpl("014", '0', ' '),
        new String[] {"9", "not-valid-authority-uiid"},
        new FieldItem().tag("014")
          .indicators(List.of("0", "\\"))
          .content("$9 not-valid-authority-uiid")
      )
    );
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
  @MethodSource("cannotProcessFields")
  void testCannotProcessField(DataField field, MarcFormat format) {
    assertFalse(converter.canProcess(field, format));
  }

  @ParameterizedTest
  @MethodSource("dataFields")
  void testCanProcessField(DataField dtoField) {
    assertTrue(converter.canProcess(dtoField, MarcFormat.BIBLIOGRAPHIC));
  }
}
