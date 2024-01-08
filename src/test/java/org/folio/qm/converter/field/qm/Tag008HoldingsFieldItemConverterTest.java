package org.folio.qm.converter.field.qm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.utils.testdata.Tag008FieldTestData;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.marc4j.marc.ControlField;

@UnitTest
class Tag008HoldingsFieldItemConverterTest {

  private final Tag008HoldingsFieldItemConverter converter = new Tag008HoldingsFieldItemConverter();

  @ParameterizedTest
  @EnumSource(value = Tag008FieldTestData.class,
              mode = EnumSource.Mode.INCLUDE,
              names = {"HOLDINGS", "HOLDINGS_NO_DATE_ENTERED"})
  void testConvertField(Tag008FieldTestData testData) {
    var fieldItem = new FieldItem().tag("008").content(testData.getQmContent());
    var actualQmField = converter.convert(fieldItem);
    assertEquals(testData.getDtoData(), ((ControlField) actualQmField).getData());
  }

  @Test
  void testCanProcessField() {
    assertTrue(converter.canProcess(new FieldItem().tag("008"), MarcFormat.HOLDINGS));
  }

  @Test
  void testCannotProcessField() {
    assertFalse(converter.canProcess(new FieldItem().tag("007"), null));
  }
}
