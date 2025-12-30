package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
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
class QuickMarcEditToQuickMarcRecordConverterTest {

  private @Mock Converter<BaseQuickMarcRecord, Record> marcConverter;
  private @Spy MarcFormatToMappingRecordTypeConverter toMappingRecordTypeConverter;
  private @Spy MarcFormatToRecordTypeConverter toRecordTypeConverter;
  private QuickMarcEditToQuickMarcRecordConverter converter;

  @BeforeEach
  void setUp() {
    converter =
      new QuickMarcEditToQuickMarcRecordConverter(marcConverter, toMappingRecordTypeConverter, toRecordTypeConverter);
  }

  @Test
  void shouldConvertQuickMarcEditToBibliographicRecord() {
    var quickMarcEdit = createQuickMarcEdit(MarcFormat.BIBLIOGRAPHIC);
    var marc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcEdit)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcEdit);

    assertNotNull(result);
    assertSame(marc4jRecord, result.getMarcRecord());
    assertSame(quickMarcEdit, result.getSource());
    assertEquals(MarcFormat.BIBLIOGRAPHIC, result.getMarcFormat());
    assertEquals(MappingRecordType.MARC_BIB, result.getMappingRecordType());
    assertEquals(RecordType.MARC_BIB, result.getSourceRecordType());
    assertEquals(quickMarcEdit.getSourceVersion(), result.getSourceVersion());
    assertEquals(quickMarcEdit.getSuppressDiscovery(), result.isSuppressDiscovery());
    assertEquals(quickMarcEdit.getExternalId(), result.getExternalId());
    assertEquals(quickMarcEdit.getExternalHrid(), result.getExternalHrid());
    assertEquals(quickMarcEdit.getParsedRecordId(), result.getParsedRecordId());
    assertEquals(quickMarcEdit.getParsedRecordDtoId(), result.getParsedRecordDtoId());

    verify(marcConverter).convert(quickMarcEdit);
    verify(toMappingRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
    verify(toRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
  }

  @Test
  void shouldConvertQuickMarcEditToAuthorityRecord() {
    var quickMarcEdit = createQuickMarcEdit(MarcFormat.AUTHORITY);
    var marc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcEdit)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcEdit);

    assertNotNull(result);
    assertEquals(MarcFormat.AUTHORITY, result.getMarcFormat());
    assertEquals(MappingRecordType.MARC_AUTHORITY, result.getMappingRecordType());
    assertEquals(RecordType.MARC_AUTHORITY, result.getSourceRecordType());
  }

  @Test
  void shouldConvertQuickMarcEditToHoldingsRecord() {
    var quickMarcEdit = createQuickMarcEdit(MarcFormat.HOLDINGS);
    var marc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcEdit)).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcEdit);

    assertNotNull(result);
    assertEquals(MarcFormat.HOLDINGS, result.getMarcFormat());
    assertEquals(MappingRecordType.MARC_HOLDINGS, result.getMappingRecordType());
    assertEquals(RecordType.MARC_HOLDING, result.getSourceRecordType());
  }

  @Test
  void shouldPreserveAllFieldsFromQuickMarcEdit() {
    var externalId = UUID.randomUUID();
    var parsedRecordId = UUID.randomUUID();
    var parsedRecordDtoId = UUID.randomUUID();

    var quickMarcEdit = new QuickMarcEdit();
    quickMarcEdit.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    quickMarcEdit.setSourceVersion(5);
    quickMarcEdit.setSuppressDiscovery(true);
    quickMarcEdit.setExternalId(externalId);
    quickMarcEdit.setExternalHrid("in00000001");
    quickMarcEdit.setParsedRecordId(parsedRecordId);
    quickMarcEdit.setParsedRecordDtoId(parsedRecordDtoId);

    var marc4jRecord = createMarcRecord();
    when(marcConverter.convert(any(QuickMarcEdit.class))).thenReturn(marc4jRecord);

    var result = converter.convert(quickMarcEdit);

    assertEquals(5, result.getSourceVersion());
    assertTrue(result.isSuppressDiscovery());
    assertEquals(externalId, result.getExternalId());
    assertEquals("in00000001", result.getExternalHrid());
    assertEquals(parsedRecordId, result.getParsedRecordId());
    assertEquals(parsedRecordDtoId, result.getParsedRecordDtoId());
  }

  @Test
  void shouldConvertMarcRecordUsingMarcConverter() {
    var quickMarcEdit = createQuickMarcEdit(MarcFormat.BIBLIOGRAPHIC);
    var expectedMarc4jRecord = createMarcRecord();

    when(marcConverter.convert(quickMarcEdit)).thenReturn(expectedMarc4jRecord);

    var result = converter.convert(quickMarcEdit);

    assertSame(expectedMarc4jRecord, result.getMarcRecord());
    verify(marcConverter).convert(quickMarcEdit);
  }

  @Test
  void shouldSetCorrectRecordTypesForBibliographic() {
    var quickMarcEdit = createQuickMarcEdit(MarcFormat.BIBLIOGRAPHIC);

    when(marcConverter.convert(any())).thenReturn(createMarcRecord());

    var result = converter.convert(quickMarcEdit);

    verify(toMappingRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
    verify(toRecordTypeConverter).convert(MarcFormat.BIBLIOGRAPHIC);
    assertEquals(MappingRecordType.MARC_BIB, result.getMappingRecordType());
    assertEquals(RecordType.MARC_BIB, result.getSourceRecordType());
  }

  private QuickMarcEdit createQuickMarcEdit(MarcFormat marcFormat) {
    var quickMarcEdit = new QuickMarcEdit();
    quickMarcEdit.setMarcFormat(marcFormat);
    quickMarcEdit.setSourceVersion(1);
    quickMarcEdit.setSuppressDiscovery(false);
    quickMarcEdit.setExternalId(UUID.randomUUID());
    quickMarcEdit.setExternalHrid("test-hrid");
    quickMarcEdit.setParsedRecordId(UUID.randomUUID());
    quickMarcEdit.setParsedRecordDtoId(UUID.randomUUID());
    return quickMarcEdit;
  }

  private Record createMarcRecord() {
    var factory = MarcFactory.newInstance();
    var newRecord = factory.newRecord();
    newRecord.setLeader(factory.newLeader("00000cam a2200000 a 4500"));
    return newRecord;
  }
}


