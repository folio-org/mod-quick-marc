package org.folio.qm.converter.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testdata.Tag008FieldTestData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.marc4j.marc.impl.LeaderImpl;

@UnitTest
class Tag008BibliographicControlFieldConverterTest {

  private final Tag008BibliographicControlFieldConverter converter = new Tag008BibliographicControlFieldConverter();

  private static Stream<Arguments> cannotProcessFields() {
    return Stream.of(
      arguments("007", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC),
      arguments("006", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC),
      arguments("035", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC),
      arguments("008", "a".repeat(39), MarcFormat.BIBLIOGRAPHIC),
      arguments("008", "a".repeat(41), MarcFormat.BIBLIOGRAPHIC),
      arguments("008", "a".repeat(40), MarcFormat.HOLDINGS),
      arguments("008", "a".repeat(40), MarcFormat.AUTHORITY)
    );
  }

  @ParameterizedTest
  @EnumSource(value = Tag008FieldTestData.class, mode = EnumSource.Mode.EXCLUDE, names = {"HOLDINGS", "AUTHORITY"})
  void testConvertField(Tag008FieldTestData testData) {
    var controlField = new ControlFieldImpl("008", testData.getDtoData());
    var leader = new LeaderImpl(testData.getLeader());
    var actualQmField = converter.convert(controlField, leader);
    assertEquals(testData.getQmContent(), actualQmField.getContent());
  }

  @ParameterizedTest
  @MethodSource("cannotProcessFields")
  void testCannotProcessField(String tag, String content, MarcFormat format) {
    assertFalse(converter.canProcess(new ControlFieldImpl(tag, content), format));
  }
}
