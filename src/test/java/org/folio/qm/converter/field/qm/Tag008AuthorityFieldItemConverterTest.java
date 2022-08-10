package org.folio.qm.converter.field.qm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testdata.Tag008FieldTestData;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.ControlField;

@UnitTest
class Tag008AuthorityFieldItemConverterTest {

  private final Tag008AuthorityFieldItemConverter converter = new Tag008AuthorityFieldItemConverter();

  @Test
  void testConvertField() {
    var fieldItem = new FieldItem().tag("008").content(Tag008FieldTestData.AUTHORITY.getQmContent());
    var actualQmField = converter.convert(fieldItem);
    assertEquals(Tag008FieldTestData.AUTHORITY.getDtoData(), ((ControlField) actualQmField).getData());
  }

  @Test
  void testCanProcessField() {
    assertTrue(converter.canProcess(new FieldItem().tag("008"), MarcFormat.AUTHORITY));
  }

  @Test
  void testCannotProcessField() {
    assertFalse(converter.canProcess(new FieldItem().tag("007"), null));
  }
}
