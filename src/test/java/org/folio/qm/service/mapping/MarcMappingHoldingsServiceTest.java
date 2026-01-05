package org.folio.qm.service.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import org.folio.Holdings;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.HoldingsRecord;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.MappingMetadataException;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.service.support.MappingMetadataProvider.MappingData;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcMappingHoldingsServiceTest {

  private @Mock MappingMetadataProvider mappingMetadataProvider;
  private @Mock FolioRecordMerger<HoldingsRecord, Holdings> merger;
  private @InjectMocks MarcMappingHoldingsService service;

  @Test
  void shouldMapNewRecord() {
    var qmRecord = createQuickMarcRecord();
    var mappingData = createMappingData();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS))
      .thenReturn(mappingData);

    var result = service.mapNewRecord(qmRecord);

    assertNotNull(result);
    verify(mappingMetadataProvider).getMappingData(MappingRecordType.MARC_HOLDINGS);
    verify(merger).merge(any(), any());
  }

  @Test
  void shouldMapUpdatedRecord() {
    var qmRecord = createQuickMarcRecord();
    var existingRecord = new HoldingsRecord();
    existingRecord.setId("existing-holdings-id");
    var mappingData = createMappingData();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS))
      .thenReturn(mappingData);

    var result = service.mapUpdatedRecord(qmRecord, existingRecord);

    assertNotNull(result);
    assertEquals("existing-holdings-id", result.getId());
    verify(mappingMetadataProvider).getMappingData(MappingRecordType.MARC_HOLDINGS);
    verify(merger).merge(any(), any());
  }

  @Test
  void shouldPreserveRecordIdAfterMapping() {
    var qmRecord = createQuickMarcRecord();
    var existingRecord = new HoldingsRecord();
    var originalId = "original-id";
    existingRecord.setId(originalId);
    var mappingData = createMappingData();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS))
      .thenReturn(mappingData);

    var result = service.mapUpdatedRecord(qmRecord, existingRecord);

    assertEquals(originalId, result.getId());
  }

  @Test
  void shouldThrowExceptionWhenMappingMetadataNotFound() {
    var qmRecord = createQuickMarcRecord();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS))
      .thenReturn(null);

    var exception = assertThrows(MappingMetadataException.class,
      () -> service.mapNewRecord(qmRecord));

    assertNotNull(exception);
    assertNotNull(exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenMappingFails() {
    var qmRecord = createQuickMarcRecord();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS))
      .thenThrow(new RuntimeException("Mapping error"));

    var exception = assertThrows(MappingMetadataException.class,
      () -> service.mapNewRecord(qmRecord));

    assertNotNull(exception);
    assertNotNull(exception.getCause());
  }

  @Test
  void shouldInitializeFolioRecord() {
    var folioRecord = service.initFolioRecord();

    assertNotNull(folioRecord);
  }

  @Test
  void shouldReturnHoldingsRecordMapper() {
    var mapper = service.getRecordMapper();

    assertNotNull(mapper);
  }

  private QuickMarcRecord createQuickMarcRecord() {
    var qmRecord = new QuickMarcRecord();
    qmRecord.setMappingRecordType(MappingRecordType.MARC_HOLDINGS);
    qmRecord.setParsedRecordId(java.util.UUID.randomUUID());
    qmRecord.setParsedContent(new JsonObject().put("leader", "00000naa a2200000"));
    return qmRecord;
  }

  private MappingData createMappingData() {
    var mappingRules = new JsonObject().put("001", new JsonObject());
    var mappingParameters = new MappingParameters();
    return new MappingData(mappingRules, mappingParameters);
  }
}
