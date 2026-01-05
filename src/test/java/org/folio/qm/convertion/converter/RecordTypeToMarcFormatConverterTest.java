package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class RecordTypeToMarcFormatConverterTest {

  private final RecordTypeToMarcFormatConverter converter = new RecordTypeToMarcFormatConverter();

  @Test
  void shouldConvertMarcBibToBibliographic() {
    var result = converter.convert(RecordType.MARC_BIB);

    assertEquals(MarcFormat.BIBLIOGRAPHIC, result);
  }

  @Test
  void shouldConvertMarcAuthorityToAuthority() {
    var result = converter.convert(RecordType.MARC_AUTHORITY);

    assertEquals(MarcFormat.AUTHORITY, result);
  }

  @Test
  void shouldConvertMarcHoldingToHoldings() {
    var result = converter.convert(RecordType.MARC_HOLDING);

    assertEquals(MarcFormat.HOLDINGS, result);
  }

  @Test
  void shouldReturnNullForUnmappedRecordType() {
    var result = converter.convert(null);

    assertNull(result);
  }
}
