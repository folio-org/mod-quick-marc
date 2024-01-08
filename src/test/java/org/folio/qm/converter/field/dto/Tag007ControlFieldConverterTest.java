package org.folio.qm.converter.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.support.utils.testdata.Tag007FieldTestData;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.marc4j.marc.impl.ControlFieldImpl;

@UnitTest
class Tag007ControlFieldConverterTest {

  private final Tag007ControlFieldConverter converter = new Tag007ControlFieldConverter();

  @ParameterizedTest
  @EnumSource(Tag007FieldTestData.class)
  void testConvertField(Tag007FieldTestData testData) {
    var actualQmField = converter.convert(new ControlFieldImpl("007", testData.getDtoData()), null);
    assertEquals(testData.getQmContent(), actualQmField.getContent());
  }

  @Test
  void testCanProcessField() {
    assertTrue(converter.canProcess(new ControlFieldImpl("007"), null));
  }

  @Test
  void testCannotProcessField() {
    assertFalse(converter.canProcess(new ControlFieldImpl("006"), null));
  }
}
