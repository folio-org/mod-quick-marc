package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class MarcFormatToRecordTypeConverterTest {

  private final MarcFormatToRecordTypeConverter converter = new MarcFormatToRecordTypeConverter();

  @Test
  void shouldConvertBibliographicToMarcBib() {
    var result = converter.convert(MarcFormat.BIBLIOGRAPHIC);

    assertEquals(RecordType.MARC_BIB, result);
  }

  @Test
  void shouldConvertAuthorityToMarcAuthority() {
    var result = converter.convert(MarcFormat.AUTHORITY);

    assertEquals(RecordType.MARC_AUTHORITY, result);
  }

  @Test
  void shouldConvertHoldingsToMarcHolding() {
    var result = converter.convert(MarcFormat.HOLDINGS);

    assertEquals(RecordType.MARC_HOLDING, result);
  }

  @Test
  void shouldReturnNullForUnmappedFormat() {
    var result = converter.convert(null);

    assertNull(result);
  }
}
