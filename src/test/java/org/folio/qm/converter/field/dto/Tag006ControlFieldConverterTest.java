package org.folio.qm.converter.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.marc4j.marc.impl.ControlFieldImpl;

import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testdata.Tag006FieldTestData;

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
