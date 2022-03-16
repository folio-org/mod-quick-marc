package org.folio.qm.converter.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.marc4j.marc.impl.LeaderImpl;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testdata.Tag008FieldTestData;

@UnitTest
class Tag008AuthorityControlFieldConverterTest {

  private final Tag008AuthorityControlFieldConverter converter = new Tag008AuthorityControlFieldConverter();

  private static Stream<Arguments> cannotProcessFields() {
    return Stream.of(
      arguments("007", "a".repeat(40), MarcFormat.AUTHORITY),
      arguments("006", "a".repeat(40), MarcFormat.AUTHORITY),
      arguments("035", "a".repeat(40), MarcFormat.AUTHORITY),
      arguments("008", "a".repeat(39), MarcFormat.AUTHORITY),
      arguments("008", "a".repeat(41), MarcFormat.AUTHORITY),
      arguments("008", "a".repeat(40), MarcFormat.HOLDINGS),
      arguments("008", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC)
    );
  }

  @Test
  void testConvertField() {
    var field = new ControlFieldImpl("008", Tag008FieldTestData.AUTHORITY.getDtoData());
    var actualQmField = converter.convert(field, new LeaderImpl(Tag008FieldTestData.AUTHORITY.getLeader()));
    assertEquals(Tag008FieldTestData.AUTHORITY.getQmContent(), actualQmField.getContent());
  }

  @Test
  void testCanProcessField() {
    var field = new ControlFieldImpl("008", "a".repeat(40));
    assertTrue(converter.canProcess(field, MarcFormat.AUTHORITY));
  }

  @ParameterizedTest
  @MethodSource("cannotProcessFields")
  void testCannotProcessField(String tag, String content, MarcFormat format) {
    assertFalse(converter.canProcess(new ControlFieldImpl(tag, content), format));
  }
}
