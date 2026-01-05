package org.folio.qm.convertion.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.spring.testing.type.UnitTest;
import org.folio.support.testdata.Tag006FieldTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.marc4j.marc.impl.ControlFieldImpl;

@UnitTest
class Tag006ControlFieldConverterTest {

  private final Tag006ControlFieldConverter converter = new Tag006ControlFieldConverter();

  @ParameterizedTest
  @EnumSource(value = Tag006FieldTestData.class)
  void testConvertField(Tag006FieldTestData testData) {
    var actualQmField = converter.convert(new ControlFieldImpl("006", testData.getDtoData()), null);
    assertEquals(testData.getQmContent(), actualQmField.getContent());
  }

  @Test
  void testCanProcessField() {
    assertTrue(converter.canProcess(new ControlFieldImpl("006"), null));
  }

  @Test
  void testCannotProcessField() {
    assertFalse(converter.canProcess(new ControlFieldImpl("007"), null));
  }
}
