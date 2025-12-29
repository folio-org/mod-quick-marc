package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

@UnitTest
@ExtendWith(MockitoExtension.class)
class QuickMarcCreateToQuickMarcRecordConverterTest {

  private @Mock Converter<BaseMarcRecord, Record> marcConverter;
  private @Spy MarcFormatToMappingRecordTypeConverter toMappingRecordTypeConverter;
  private @Spy MarcFormatToRecordTypeConverter toRecordTypeConverter;
  private QuickMarcCreateToQuickMarcRecordConverter converter;

  @BeforeEach
  void setUp() {
    converter = new QuickMarcCreateToQuickMarcRecordConverter(marcConverter, toMappingRecordTypeConverter,
      toRecordTypeConverter);
  }

  @Test
  void shouldConvertQuickMarcCreateToBibliographicRecord() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.BIBLIOGRAPHIC);
    var marc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcCreate)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertNotNull(result);
    assertSame(marc4jRecord, result.getMarcRecord());
    assertSame(quickMarcCreate, result.getSource());
    assertEquals(MarcFormat.BIBLIOGRAPHIC, result.getMarcFormat());
    assertEquals(MappingRecordType.MARC_BIB, result.getMappingRecordType());
    assertEquals(RecordType.MARC_BIB, result.getSrsRecordType());
    assertFalse(result.isSuppressDiscovery());

    verify(marcConverter).convert(quickMarcCreate);
    verify(toMappingRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
    verify(toRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
  }

  @Test
  void shouldConvertQuickMarcCreateToAuthorityRecord() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.AUTHORITY);
    var marc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcCreate)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertNotNull(result);
    assertEquals(MarcFormat.AUTHORITY, result.getMarcFormat());
    assertEquals(MappingRecordType.MARC_AUTHORITY, result.getMappingRecordType());
    assertEquals(RecordType.MARC_AUTHORITY, result.getSrsRecordType());
  }

  @Test
  void shouldConvertQuickMarcCreateToHoldingsRecord() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.HOLDINGS);
    var marc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcCreate)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertNotNull(result);
    assertEquals(MarcFormat.HOLDINGS, result.getMarcFormat());
    assertEquals(MappingRecordType.MARC_HOLDINGS, result.getMappingRecordType());
    assertEquals(RecordType.MARC_HOLDING, result.getSrsRecordType());
  }

  @Test
  void shouldPreserveSuppressDiscoveryTrue() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    quickMarcCreate.setSuppressDiscovery(true);

    var marc4jRecord = createMarcRecord();
    when(marcConverter.convert(any(QuickMarcCreate.class))).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertTrue(result.isSuppressDiscovery());
  }

  @Test
  void shouldPreserveSuppressDiscoveryFalse() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    quickMarcCreate.setSuppressDiscovery(false);

    var marc4jRecord = createMarcRecord();
    when(marcConverter.convert(any(QuickMarcCreate.class))).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertFalse(result.isSuppressDiscovery());
  }

  @Test
  void shouldConvertMarcRecordUsingMarcConverter() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.BIBLIOGRAPHIC);
    var expectedMarc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcCreate)).thenReturn(expectedMarc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertSame(expectedMarc4jRecord, result.getMarcRecord());
    verify(marcConverter).convert(quickMarcCreate);
  }

  @Test
  void shouldSetCorrectRecordTypesForBibliographic() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.BIBLIOGRAPHIC);

    when(marcConverter.convert(any())).thenReturn(createMarcRecord());

    var result = converter.convert(quickMarcCreate);

    verify(toMappingRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
    verify(toRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
    assertEquals(MappingRecordType.MARC_BIB, result.getMappingRecordType());
    assertEquals(RecordType.MARC_BIB, result.getSrsRecordType());
  }

  @Test
  void shouldSetCorrectRecordTypesForAuthority() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.AUTHORITY);

    when(marcConverter.convert(any())).thenReturn(createMarcRecord());

    var result = converter.convert(quickMarcCreate);

    verify(toMappingRecordTypeConverter).convert(MarcFormat.AUTHORITY);
    verify(toRecordTypeConverter).convert(MarcFormat.AUTHORITY);
    assertEquals(MappingRecordType.MARC_AUTHORITY, result.getMappingRecordType());
    assertEquals(RecordType.MARC_AUTHORITY, result.getSrsRecordType());
  }

  @Test
  void shouldSetCorrectRecordTypesForHoldings() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.HOLDINGS);

    when(marcConverter.convert(any())).thenReturn(createMarcRecord());

    var result = converter.convert(quickMarcCreate);

    verify(toMappingRecordTypeConverter).convert(MarcFormat.HOLDINGS);
    verify(toRecordTypeConverter).convert(MarcFormat.HOLDINGS);
    assertEquals(MappingRecordType.MARC_HOLDINGS, result.getMappingRecordType());
    assertEquals(RecordType.MARC_HOLDING, result.getSrsRecordType());
  }

  @Test
  void shouldNotSetExternalIds() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.BIBLIOGRAPHIC);

    when(marcConverter.convert(any())).thenReturn(createMarcRecord());

    var result = converter.convert(quickMarcCreate);

    assertNull(result.getExternalId());
    assertNull(result.getExternalHrid());
    assertNull(result.getParsedRecordId());
    assertNull(result.getParsedRecordDtoId());
    assertNull(result.getSourceVersion());
  }

  @Test
  void shouldSetSourceToQuickMarcCreate() {
    var quickMarcCreate = createQuickMarcCreate(MarcFormat.BIBLIOGRAPHIC);
    var marc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcCreate)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertNotNull(result.getSource());
    assertSame(quickMarcCreate, result.getSource());
  }

  @Test
  void shouldConvertWithAllRequiredFields() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    quickMarcCreate.setSuppressDiscovery(true);

    var marc4jRecord = createMarcRecord();
    when(marcConverter.convert(quickMarcCreate)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcCreate);

    assertNotNull(result);
    assertNotNull(result.getSource());
    assertNotNull(result.getMarcRecord());
    assertNotNull(result.getMarcFormat());
    assertNotNull(result.getMappingRecordType());
    assertNotNull(result.getSrsRecordType());
  }

  private QuickMarcCreate createQuickMarcCreate(MarcFormat marcFormat) {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(marcFormat);
    quickMarcCreate.setSuppressDiscovery(false);
    return quickMarcCreate;
  }

  private Record createMarcRecord() {
    var factory = MarcFactory.newInstance();
    var newRecord = factory.newRecord();
    newRecord.setLeader(factory.newLeader("00000cam a2200000 a 4500"));
    return newRecord;
  }
}
