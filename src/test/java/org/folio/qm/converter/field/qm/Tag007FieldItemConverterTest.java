package org.folio.qm.converter.field.qm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.testdata.Tag007FieldTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.marc4j.marc.ControlField;

@UnitTest
class Tag007FieldItemConverterTest {

  private final Tag007FieldItemConverter converter = new Tag007FieldItemConverter();

  @ParameterizedTest
  @EnumSource(value = Tag007FieldTestData.class)
  void testConvertField(Tag007FieldTestData testData) {
    var actualQmField = converter.convert(new FieldItem().tag("007").content(testData.getQmContent()));
    assertEquals(testData.getDtoData(), ((ControlField) actualQmField).getData());
  }

  @Test
  void testCanProcessField() {
    assertTrue(converter.canProcess(new FieldItem().tag("007"), null));
  }

  @Test
  void testCannotProcessField() {
    assertFalse(converter.canProcess(new FieldItem().tag("008"), null));
  }
}
