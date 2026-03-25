package org.folio.qm.convertion.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.testdata.Tag008FieldTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.marc4j.marc.impl.LeaderImpl;

@UnitTest
class Tag008HoldingsControlFieldConverterTest {

  private final Tag008HoldingsControlFieldConverter converter = new Tag008HoldingsControlFieldConverter();

  @ParameterizedTest
  @EnumSource(value = Tag008FieldTestData.class,
              names = {"HOLDINGS", "HOLDINGS_WITH_GT_LEN", "HOLDINGS_WITH_LT_LEN"},
              mode = EnumSource.Mode.INCLUDE)
  void testConvertField(Tag008FieldTestData tag008FieldTestData) {
    var field = new ControlFieldImpl("008", tag008FieldTestData.getDtoData());
    var actualQmField = converter.convert(field, new LeaderImpl(tag008FieldTestData.getLeader()));
    assertEquals(tag008FieldTestData.getQmContent(), actualQmField.getContent());
  }

  @Test
  void testCanProcessField() {
    var field = new ControlFieldImpl("008", "a".repeat(32));
    assertTrue(converter.canProcess(field, MarcFormat.HOLDINGS));
  }

  @ParameterizedTest
  @MethodSource("cannotProcessFields")
  void testCannotProcessField(String tag, String content, MarcFormat format) {
    assertFalse(converter.canProcess(new ControlFieldImpl(tag, content), format));
  }

  private static Stream<Arguments> cannotProcessFields() {
    return Stream.of(
      arguments("007", "a".repeat(32), MarcFormat.HOLDINGS),
      arguments("006", "a".repeat(32), MarcFormat.HOLDINGS),
      arguments("035", "a".repeat(32), MarcFormat.HOLDINGS),
      arguments("008", "a".repeat(32), MarcFormat.AUTHORITY),
      arguments("008", "a".repeat(32), MarcFormat.BIBLIOGRAPHIC)
    );
  }
}
