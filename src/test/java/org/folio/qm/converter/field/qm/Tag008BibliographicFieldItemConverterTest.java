package org.folio.qm.converter.field.qm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testdata.Tag008FieldTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.marc4j.marc.ControlField;

@UnitTest
class Tag008BibliographicFieldItemConverterTest {

  private final Tag008BibliographicFieldItemConverter converter = new Tag008BibliographicFieldItemConverter();

  @ParameterizedTest
  @EnumSource(value = Tag008FieldTestData.class,
              mode = EnumSource.Mode.EXCLUDE,
              names = {"HOLDINGS", "HOLDINGS_NO_DATE_ENTERED", "HOLDINGS_WITH_GT_LEN", "HOLDINGS_WITH_LT_LEN",
                       "AUTHORITY", "AUTHORITY_WITH_GT_LEN", "AUTHORITY_WITH_LT_LEN",
                       "BIB_BOOKS_WITH_LT_LEN"})
  void testConvertField(Tag008FieldTestData testData) {
    var actualQmField = converter.convert(new FieldItem().tag("007").content(testData.getQmContent()));
    assertEquals(testData.getDtoData(), ((ControlField) actualQmField).getData());
  }

  @Test
  void testCanProcessField() {
    assertTrue(converter.canProcess(new FieldItem().tag("008"), MarcFormat.BIBLIOGRAPHIC));
  }

  @Test
  void testCannotProcessField() {
    assertFalse(converter.canProcess(new FieldItem().tag("007"), null));
  }
}
