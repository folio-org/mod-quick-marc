package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class MarcFormatToMappingRecordTypeConverterTest {

  private final MarcFormatToMappingRecordTypeConverter converter = new MarcFormatToMappingRecordTypeConverter();

  @Test
  void shouldConvertBibliographicToMarcBib() {
    var result = converter.convert(MarcFormat.BIBLIOGRAPHIC);

    assertEquals(MappingRecordType.MARC_BIB, result);
  }

  @Test
  void shouldConvertAuthorityToMarcAuthority() {
    var result = converter.convert(MarcFormat.AUTHORITY);

    assertEquals(MappingRecordType.MARC_AUTHORITY, result);
  }

  @Test
  void shouldConvertHoldingsToMarcHoldings() {
    var result = converter.convert(MarcFormat.HOLDINGS);

    assertEquals(MappingRecordType.MARC_HOLDINGS, result);
  }

  @Test
  void shouldReturnNullForUnmappedFormat() {
    var result = converter.convert(null);

    assertNull(result);
  }
}
