package org.folio.qm.converter.field.qm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testdata.Tag006FieldTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.marc4j.marc.ControlField;

@UnitTest
class Tag006FieldItemConverterTest {

  private final Tag006FieldItemConverter converter = new Tag006FieldItemConverter();

  @ParameterizedTest
  @EnumSource(value = Tag006FieldTestData.class)
  void testConvertField(Tag006FieldTestData testData) {
    var actualQmField = converter.convert(new FieldItem().tag("006").content(testData.getQmContent()));
    assertEquals(testData.getDtoData(), ((ControlField) actualQmField).getData());
  }

  @Test
  void testCanProcessField() {
    assertTrue(converter.canProcess(new FieldItem().tag("006"), null));
  }

  @Test
  void testCannotProcessField() {
    assertFalse(converter.canProcess(new FieldItem().tag("007"), null));
  }
}
