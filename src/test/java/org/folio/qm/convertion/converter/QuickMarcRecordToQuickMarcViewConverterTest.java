package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.folio.qm.convertion.field.MarcFieldsConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.FolioRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class QuickMarcRecordToQuickMarcViewConverterTest {

  @Mock
  private MarcFieldsConverter fieldsConverter;

  @InjectMocks
  private QuickMarcRecordToQuickMarcViewConverter converter;

  @Test
  @SuppressWarnings("checkstyle:methodLength")
  void shouldConvertQuickMarcRecordToQuickMarcView() {
    var parsedRecordId = UUID.randomUUID();
    var parsedRecordDtoId = UUID.randomUUID();
    var externalId = UUID.randomUUID().toString();
    var externalHrid = "in00000001";
    var sourceVersion = 5;

    var marcRecord = createMarcRecord("00000nam  2200000 a 4500");
    var folioRecord = createFolioRecord(externalId, externalHrid);
    var fields = List.of(
      new FieldItem().tag("001").content("test001"),
      new FieldItem().tag("245").content("$a Test title")
    );

    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .parsedRecordId(parsedRecordId)
      .parsedRecordDtoId(parsedRecordDtoId)
      .sourceVersion(sourceVersion)
      .folioRecord(folioRecord)
      .suppressDiscovery(false)
      .build();

    when(fieldsConverter.convertDtoFields(anyList(), any(Leader.class), eq(MarcFormat.BIBLIOGRAPHIC)))
      .thenReturn(fields);

    var result = converter.convert(quickMarcRecord);

    assertNotNull(result);
    assertEquals("00000nam\\\\2200000\\a\\4500", result.getLeader());
    assertEquals(MarcFormat.BIBLIOGRAPHIC, result.getMarcFormat());
    assertEquals(parsedRecordId, result.getParsedRecordId());
    assertEquals(parsedRecordDtoId, result.getParsedRecordDtoId());
    assertEquals(sourceVersion, result.getSourceVersion());
    assertEquals(UUID.fromString(externalId), result.getExternalId());
    assertEquals(externalHrid, result.getExternalHrid());
    assertFalse(result.getSuppressDiscovery());
    assertEquals(fields, result.getFields());
  }

  @Test
  void shouldConvertWithSuppressDiscoveryTrue() {
    var marcRecord = createMarcRecord("00000nam a2200000 a 4500");
    var folioRecord = createFolioRecord("123e4567-e89b-12d3-a456-426614174000", "test-hrid");

    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .folioRecord(folioRecord)
      .suppressDiscovery(true)
      .build();

    when(fieldsConverter.convertDtoFields(any(), any(), any())).thenReturn(Collections.emptyList());

    var result = converter.convert(quickMarcRecord);

    assertTrue(result.getSuppressDiscovery());
  }

  @Test
  void shouldConvertAuthorityFormat() {
    var marcRecord = createMarcRecord("00000nz  a2200000 a 4500");
    var folioRecord = createFolioRecord("123e4567-e89b-12d3-a456-426614174001", "auth-hrid");

    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .marcFormat(MarcFormat.AUTHORITY)
      .folioRecord(folioRecord)
      .suppressDiscovery(false)
      .build();

    when(fieldsConverter.convertDtoFields(any(), any(), eq(MarcFormat.AUTHORITY)))
      .thenReturn(Collections.emptyList());

    var result = converter.convert(quickMarcRecord);

    assertEquals(MarcFormat.AUTHORITY, result.getMarcFormat());
    verify(fieldsConverter).convertDtoFields(any(), any(), eq(MarcFormat.AUTHORITY));
  }

  @Test
  void shouldConvertHoldingsFormat() {
    var marcRecord = createMarcRecord("00000nx  a2200000 a 4500");
    var folioRecord = createFolioRecord("123e4567-e89b-12d3-a456-426614174002", "hold-hrid");

    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .marcFormat(MarcFormat.HOLDINGS)
      .folioRecord(folioRecord)
      .suppressDiscovery(false)
      .build();

    when(fieldsConverter.convertDtoFields(any(), any(), eq(MarcFormat.HOLDINGS)))
      .thenReturn(Collections.emptyList());

    var result = converter.convert(quickMarcRecord);

    assertEquals(MarcFormat.HOLDINGS, result.getMarcFormat());
    verify(fieldsConverter).convertDtoFields(any(), any(), eq(MarcFormat.HOLDINGS));
  }

  @Test
  void shouldConvertWithEmptyFields() {
    var marcRecord = createMarcRecord("00000nam a2200000 a 4500");
    var folioRecord = createFolioRecord("123e4567-e89b-12d3-a456-426614174000", "test");

    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .folioRecord(folioRecord)
      .suppressDiscovery(false)
      .build();

    when(fieldsConverter.convertDtoFields(any(), any(), any())).thenReturn(Collections.emptyList());

    var result = converter.convert(quickMarcRecord);

    assertNotNull(result.getFields());
    assertTrue(result.getFields().isEmpty());
  }

  private Record createMarcRecord(String leaderString) {
    var factory = MarcFactory.newInstance();
    var newRecord = factory.newRecord();
    var leader = factory.newLeader(leaderString);
    newRecord.setLeader(leader);
    return newRecord;
  }

  private FolioRecord createFolioRecord(String id, String hrid) {
    return new FolioRecord() {
      private String recordId = id;

      @Override
      public String getId() {
        return recordId;
      }

      @Override
      public void setId(String id) {
        this.recordId = id;
      }

      @Override
      public String getHrid() {
        return hrid;
      }
    };
  }
}
