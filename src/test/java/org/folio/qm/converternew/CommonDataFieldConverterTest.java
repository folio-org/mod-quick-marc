package org.folio.qm.converternew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.DataField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;

import org.folio.qm.converternew.dto.CommonDataFieldConverter;
import org.folio.qm.domain.dto.FieldItem;

class CommonDataFieldConverterTest {

  private final CommonDataFieldConverter converter = new CommonDataFieldConverter();

  private static Stream<Arguments> validFieldData() {
    return Stream.of(
      arguments(new DataFieldImpl("948", '1', '2'),
        new String[]{"a", "1", "b", "a", "d", "b", "e", "2", "1", "3"},
        new FieldItem().tag("948").indicators(List.of("1", "2")).content("$a 1 $b a $d b $e 2 $1 3")
      ),
      arguments(new DataFieldImpl("010", '1', '1'),
        new String[]{"a", " 2001000234"},
        new FieldItem().tag("010").indicators(List.of("1", "1")).content("$a  2001000234")
      ),
      arguments(new DataFieldImpl("010", '1', '1'),
        new String[]{"a", "sn2003045678 "},
        new FieldItem().tag("010").indicators(List.of("1", "1")).content("$a sn2003045678 ")
      ),
      arguments(new DataFieldImpl("010", '1', '1'),
        new String[]{"a", "34005678 "},
        new FieldItem().tag("010").indicators(List.of("1", "1")).content("$a 34005678 ")
      ),
      arguments(new DataFieldImpl("010", ' ', ' '),
        new String[]{"a", "  34005678 /M"},
        new FieldItem().tag("010").indicators(List.of("\\", "\\")).content("$a   34005678 /M")
      )
    );
  }

  private static Stream<Arguments> invalidFieldData() {
    return Stream.of(
      arguments(new DataFieldImpl("010", ' ', ' '),
        new String[]{"a", ""}
      ),
      arguments(new DataFieldImpl("010", ' ', ' '),
        new String[]{"aa", "2002003456  "}
      )
    );
  }

//  @ParameterizedTest
//  @MethodSource("validFieldData")
//  void name2(DataField dtoField, String[] dtoContent, FieldItem expectedQmField) {
//    for (int i = 0; i < dtoContent.length; i += 2) {
//      dtoField.addSubfield(new SubfieldImpl(dtoContent[i].charAt(0), dtoContent[i + 1]));
//    }
//    var actualQmField = converter.convert(dtoField, );
//    assertEquals(expectedQmField.toString(), actualQmField.toString());
//  }

}
