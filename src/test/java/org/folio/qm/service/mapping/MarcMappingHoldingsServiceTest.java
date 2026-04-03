package org.folio.qm.service.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.HoldingsFolioRecord;
import org.folio.qm.domain.model.InstanceFolioRecord;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.MappingMetadataException;
import org.folio.qm.service.storage.folio.FolioRecordInstanceService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.service.support.MappingMetadataProvider.MappingData;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.MarcFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcMappingHoldingsServiceTest {

  private @Mock MappingMetadataProvider mappingMetadataProvider;
  private @Mock FolioRecordMerger<HoldingsFolioRecord, HoldingsRecord> merger;
  private @Mock FolioRecordInstanceService folioRecordInstanceService;
  private @InjectMocks MarcMappingHoldingsService service;

  @Test
  void shouldMapNewRecord() {
    var mappingData = createMappingData();
    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS))
      .thenReturn(mappingData);
    var instanceRecord = new InstanceFolioRecord();
    var instanceId = UUID.randomUUID().toString();
    instanceRecord.setId(instanceId);
    when(folioRecordInstanceService.getInstanceIdByHrid(any())).thenReturn(instanceId);
    doAnswer(invocation -> {
      HoldingsFolioRecord holdingsRecord = invocation.getArgument(1);
      holdingsRecord.setInstanceId(instanceId);
      return null;
    }).when(merger).merge(any(), any());

    var qmRecord = createQuickMarcRecord();

    var result = service.mapNewRecord(qmRecord);

    assertNotNull(result);
    assertEquals(instanceId, result.getInstanceId());
    verify(mappingMetadataProvider).getMappingData(MappingRecordType.MARC_HOLDINGS);
    verify(merger).merge(any(), any());
  }

  @Test
  void shouldMapUpdatedRecord() {
    var qmRecord = createQuickMarcRecord();
    var existingRecord = new HoldingsFolioRecord();
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
  void mapRequiredFields_shouldSetInstanceId_when004Present() {
    var qmRecord = createQuickMarcRecord();
    var holdings = new HoldingsRecord();
    var instanceId = UUID.randomUUID().toString();
    when(folioRecordInstanceService.getInstanceIdByHrid("instanceHrid")).thenReturn(instanceId);

    service.mapRequiredFields(qmRecord, holdings);

    assertEquals(instanceId, holdings.getInstanceId());
  }

  @Test
  void mapRequiredFields_shouldThrowException_when004Missing() {
    var qmRecord = createQuickMarcRecord();
    // Remove 004 field
    qmRecord.getMarcRecord().getControlFields().removeIf(f -> "004".equals(f.getTag()));
    var holdings = new HoldingsRecord();

    assertThrows(IllegalStateException.class, () -> service.mapRequiredFields(qmRecord, holdings));
  }

  @Test
  void shouldPreserveRecordIdAfterMapping() {
    var qmRecord = createQuickMarcRecord();
    var existingRecord = new HoldingsFolioRecord();
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
    var marcRecord = MarcFactory.newInstance().newRecord();
    marcRecord.addVariableField(MarcFactory.newInstance().newControlField("004", "instanceHrid"));
    qmRecord.setMarcRecord(marcRecord);
    qmRecord.setMappingRecordType(MappingRecordType.MARC_HOLDINGS);
    qmRecord.setParsedRecordId(java.util.UUID.randomUUID());
    qmRecord.setParsedContent(new JsonObject()
      .put("leader", "01510cz  a2200313n  4500")
      .put("fields", List.of(
        new JsonObject().put("004", "instanceHrid"),
        new JsonObject().put("001", "holdingsHrid")
      ))
    );
    return qmRecord;
  }

  private MappingData createMappingData() {
    var mappingRules = new JsonObject().put("001", List.of(
      new JsonObject()
        .put("rules", List.of())
        .put("target", "hrid")
        .put("subfield", List.of())
        .put("description", "HRID")
    ));

    var mappingParameters = new MappingParameters();
    return new MappingData(mappingRules, mappingParameters);
  }
}
