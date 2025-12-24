//package org.folio.qm.service.impl;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.ArgumentMatchers.isA;
//import static org.mockito.Mockito.lenient;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import io.vertx.core.json.JsonObject;
//import java.util.UUID;
//import org.folio.Metadata;
//import org.folio.ParsedRecord;
//import org.folio.RawRecord;
//import org.folio.Record;
//import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
//import org.folio.qm.client.HoldingsStorageClient;
//import org.folio.qm.client.SourceStorageClient;
//import org.folio.qm.client.model.MappingRecordType;
//import org.folio.qm.client.model.RecordTypeEnum;
//import org.folio.qm.converter.MarcQmConverter;
//import org.folio.qm.domain.dto.MarcFormat;
//import org.folio.qm.domain.dto.QuickMarcEdit;
//import org.folio.qm.exception.MappingMetadataException;
//import org.folio.qm.mapper.HoldingsRecordMapper;
//import org.folio.qm.mapper.MarcTypeMapper;
//import org.folio.qm.service.support.MappingMetadataProvider;
//import org.folio.rest.jaxrs.model.HoldingsRecord;
//import org.folio.spring.FolioExecutionContext;
//import org.folio.spring.exception.NotFoundException;
//import org.folio.spring.testing.type.UnitTest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@UnitTest
//@ExtendWith(MockitoExtension.class)
//class HoldingRecordServiceTest {
//
//  private static final String HOLDING_ID = UUID.randomUUID().toString();
//  private static final String PARSED_RECORD_ID = UUID.randomUUID().toString();
//  private static final String CALL_NUMBER = "Call Number 123";
//  private static final String CONTENT = "{\"leader\":\"00000nu  a2200000n  4500\"}";
//  private static final String SNAPSHOT_ID = UUID.randomUUID().toString();
//  private final UUID parsedRecordId = UUID.fromString(PARSED_RECORD_ID);
//
//  @Mock
//  private MappingMetadataProvider mappingMetadataProvider;
//  @Mock
//  private SourceStorageClient sourceStorageClient;
//  @Mock
//  private HoldingsStorageClient holdingsStorageClient;
//  @Mock
//  private HoldingsRecordMapper mapper;
//  @Mock
//  private FolioExecutionContext folioExecutionContext;
//  @Mock
//  private MarcQmConverter<QuickMarcEdit> marcQmConverter;
//  @Mock
//  private MarcTypeMapper typeMapper;
//
//  @InjectMocks
//  private HoldingsRecordService service;
//
//  private QuickMarcEdit quickMarc;
//  private HoldingsRecord existingHolding;
//
//  @BeforeEach
//  void setUp() {
//    lenient().when(mappingMetadataProvider.getMappingData(isA(String.class)))
//      .thenReturn(new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters()));
//    lenient().when(folioExecutionContext.getUserId()).thenReturn(UUID.randomUUID());
//    var objectMapper = new ObjectMapper();
//    ObjectNode parsedNode = objectMapper.createObjectNode();
//    parsedNode.put("leader", "00000nam  a2200000n  4500");
//    lenient().when(marcQmConverter.convertToParsedContent(any(QuickMarcEdit.class))).thenReturn(parsedNode);
//    lenient().when(typeMapper.toDto(any(MarcFormat.class))).thenReturn(RecordTypeEnum.HOLDING);
//
//    quickMarc = createQuickMarc();
//    existingHolding = createHolding();
//  }
//
//  @Test
//  void shouldReturnHoldingSupportedType() {
//    assertEquals(MarcFormat.HOLDINGS, service.supportedType());
//  }
//
//  @Test
//  void shouldSuccessfullyUpdateHolding() {
//    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS.getValue()))
//      .thenReturn(mappingData);
//    when(holdingsStorageClient.getHoldingById(HOLDING_ID)).thenReturn(existingHolding);
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//
//    service.update(quickMarc);
//
//    verify(holdingsStorageClient).updateHolding(eq(HOLDING_ID), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//  }
//
//  @Test
//  void shouldThrowMappingMetadataException_whenMappingMetadataNotFound() {
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS.getValue())).thenReturn(null);
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//
//    var exception = assertThrows(MappingMetadataException.class, () ->
//      service.update(quickMarc)
//    );
//
//    assertEquals(String.format("mapping metadata not found for %s record with parsedRecordId: %s",
//      MappingRecordType.MARC_HOLDINGS.getValue(), parsedRecordId), exception.getMessage());
//    verify(holdingsStorageClient, never()).getHoldingById(anyString());
//    verify(holdingsStorageClient, never()).updateHolding(anyString(), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//  }
//
//  @Test
//  void shouldThrowNotFoundException_whenHoldingNotFound() {
//    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS.getValue()))
//      .thenReturn(mappingData);
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    when(holdingsStorageClient.getHoldingById(HOLDING_ID)).thenReturn(null);
//
//    var exception = assertThrows(NotFoundException.class, () ->
//      service.update(quickMarc)
//    );
//
//    verify(holdingsStorageClient, never()).updateHolding(anyString(), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//    assertEquals(String.format("Holdings record with id: %s not found", HOLDING_ID), exception.getMessage());
//  }
//
//  @Test
//  void shouldThrowNotFoundException_whenSrsRecordNotFound() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(null);
//
//    var exception = assertThrows(NotFoundException.class, () ->
//      service.update(quickMarc)
//    );
//
//    verify(holdingsStorageClient, never()).updateHolding(eq(HOLDING_ID), any());
//    verify(sourceStorageClient, never()).updateSrsRecord(anyString(), any());
//    assertEquals(String.format("The SRS record to update was not found for parsedRecordId: %s", parsedRecordId),
//      exception.getMessage());
//  }
//
//  @Test
//  void shouldThrowRuntimeException_whenMappingMetadataRetrievalFails() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_HOLDINGS.getValue()))
//      .thenThrow(new RuntimeException("Unexpected error"));
//
//    var exception = assertThrows(RuntimeException.class, () ->
//      service.update(quickMarc)
//    );
//
//    verify(holdingsStorageClient, never()).updateHolding(anyString(), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any());
//    assertEquals(String.format("Error mapping %s record with parsedRecordId: %s",
//      MappingRecordType.MARC_HOLDINGS.getValue(), parsedRecordId), exception.getMessage());
//  }
//
//  private QuickMarcEdit createQuickMarc() {
//    var quick = new QuickMarcEdit();
//    quick.setParsedRecordDtoId(UUID.fromString(PARSED_RECORD_ID));
//    quick.setParsedRecordId(UUID.fromString(PARSED_RECORD_ID));
//    quick.setExternalId(UUID.fromString(HOLDING_ID));
//    quick.setExternalHrid("hrid-1");
//    quick.setSuppressDiscovery(false);
//    quick.setMarcFormat(MarcFormat.HOLDINGS);
//    quick.setLeader("00000nam  a2200000n  4500");
//
//    var field = new org.folio.qm.domain.dto.FieldItem();
//    field.setTag("245");
//    field.setContent("$a Test Title");
//    quick.addFieldsItem(field);
//    return quick;
//  }
//
//  private HoldingsRecord createHolding() {
//    var holding = new HoldingsRecord();
//    holding.setId(HOLDING_ID);
//    holding.setCallNumber(CALL_NUMBER);
//    holding.setVersion(1L);
//    return holding;
//  }
//
//  private Record createRecord() {
//    var srsRecord = new Record();
//    srsRecord.setId(PARSED_RECORD_ID);
//    srsRecord.setSnapshotId(SNAPSHOT_ID);
//    srsRecord.setRecordType(Record.RecordType.MARC_HOLDING);
//    srsRecord.setGeneration(0);
//    srsRecord.setOrder(1);
//    srsRecord.setRawRecord(new RawRecord().withId(PARSED_RECORD_ID).withContent("raw content"));
//    srsRecord.setMetadata(new Metadata());
//
//    var parsedRecord = new ParsedRecord();
//    parsedRecord.setId(PARSED_RECORD_ID);
//    parsedRecord.setContent(CONTENT);
//    srsRecord.setParsedRecord(parsedRecord);
//
//    return srsRecord;
//  }
//}
