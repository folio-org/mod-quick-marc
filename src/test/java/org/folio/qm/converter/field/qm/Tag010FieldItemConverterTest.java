package org.folio.qm.converter.field.qm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.ControlField;

@UnitTest
class Tag010FieldItemConverterTest {

  private final Tag010FieldItemConverter converter = new Tag010FieldItemConverter();

  private static Stream<Arguments> fieldData() {
    return Stream.of(
      arguments("$a  2001000234", "  $a  2001000234"),
      arguments("$aa 2002003456  ", "  $aa 2002003456"),
      arguments("$a sn2003045678 ", "  $asn2003045678"),
      arguments("$a 34005678", "  $a   34005678 "),
      arguments("$a   34005678 /M", "  $a   34005678 /M"),
      arguments("$ae  45000067 ", "  $ae  45000067 "),
      arguments("$a  sn 85000678 ", "  $asn 85000678 "),
      arguments("$a agr25000003  ", "  $aagr25000003 "),
      arguments("$aagr25000003 /M", "  $aagr25000003 /M")
    );
  }

  @ParameterizedTest
  @MethodSource("fieldData")
  void testConvertField(String qmContent, String dtoContent) {
    var actualDtoField = converter.convert(new FieldItem().tag("010").content(qmContent));

    assertThat(actualDtoField)
      .isNotNull()
      .isInstanceOf(ControlField.class);

    var controlField = (ControlField) actualDtoField;
    assertThat(controlField.getTag()).isEqualTo("010");
    assertThat(controlField.getData()).isEqualTo(dtoContent);
  }

  @Test
  void testCanProcess() {
    assertTrue(converter.canProcess(new FieldItem().tag("010"), null));
  }

  @Test
  void testCannotProcess() {
    assertFalse(converter.canProcess(new FieldItem().tag("011"), null));
  }
}
