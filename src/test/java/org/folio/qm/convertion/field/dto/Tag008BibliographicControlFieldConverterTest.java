package org.folio.qm.convertion.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.testdata.Tag008FieldTestData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.marc4j.marc.impl.LeaderImpl;

@UnitTest
class Tag008BibliographicControlFieldConverterTest {

  private final Tag008BibliographicControlFieldConverter converter = new Tag008BibliographicControlFieldConverter();

  //some bib test data excluded because it's for another direction conversion with replacements
  @ParameterizedTest
  @EnumSource(value = Tag008FieldTestData.class,
              mode = EnumSource.Mode.EXCLUDE,
              names = {"BIB_BOOKS_NO_DATE_ENTERED", "BIB_BOOKS_DATE_ENTERED_ALPHABETIC", "BIB_BOOKS_DATE_ENTERED_SHORT",
                       "BIB_BOOKS_DATE_ENTERED_INVALID",
                       "HOLDINGS", "HOLDINGS_NO_DATE_ENTERED", "HOLDINGS_WITH_GT_LEN", "HOLDINGS_WITH_LT_LEN",
                       "AUTHORITY", "AUTHORITY_NO_DATE_ENTERED", "AUTHORITY_WITH_GT_LEN", "AUTHORITY_WITH_LT_LEN"})
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

  private static Stream<Arguments> cannotProcessFields() {
    return Stream.of(
      arguments("007", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC),
      arguments("006", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC),
      arguments("035", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC),
      arguments("008", "a".repeat(40), MarcFormat.HOLDINGS),
      arguments("008", "a".repeat(40), MarcFormat.AUTHORITY)
    );
  }
}
