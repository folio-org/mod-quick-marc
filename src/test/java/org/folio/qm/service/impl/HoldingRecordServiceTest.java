package org.folio.qm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import java.util.UUID;
import org.folio.Metadata;
import org.folio.ParsedRecord;
import org.folio.Record;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.qm.client.HoldingsStorageClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.ExternalIdsHolder;
import org.folio.qm.client.model.HoldingsRecord;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.mapper.ExternalIdsHolderMapper;
import org.folio.qm.mapper.HoldingsRecordMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HoldingRecordServiceTest {

  private static final String HOLDING_ID = UUID.randomUUID().toString();
  private static final String PARSED_RECORD_ID = UUID.randomUUID().toString();
  private static final String CALL_NUMBER = "Call Number 123";
  private static final String CONTENT = "{\"leader\":\"00000nu  a2200000n  4500\"}";
  private static final String SNAPSHOT_ID = UUID.randomUUID().toString();

  @Mock
  private MappingMetadataProvider mappingMetadataProvider;
  @Mock
  private SourceStorageClient sourceStorageClient;
  @Mock
  private ExternalIdsHolderMapper externalIdsHolderMapper;
  @Mock
  private HoldingsStorageClient holdingsStorageClient;
  @Mock
  private HoldingsRecordMapper mapper;
  @Mock
  private FolioExecutionContext folioExecutionContext;

  @InjectMocks
  private HoldingRecordService service;

  private ParsedRecordDto parsedRecordDto;
  private DeferredResult<ResponseEntity<Void>> updateResult;
  private HoldingsRecord existingHolding;

  @BeforeEach
  void setUp() {
    lenient().when(mappingMetadataProvider.getMappingData(String.valueOf(isA(MappingRecordTypeEnum.class))))
      .thenReturn(new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters()));
    lenient().when(folioExecutionContext.getUserId()).thenReturn(UUID.randomUUID());
    updateResult = new DeferredResult<>();
    parsedRecordDto = createParsedRecordDto();
    existingHolding = createHolding();
  }

  @Test
  void shouldReturnHoldingSupportedType() {
    assertEquals(RecordTypeEnum.HOLDING, service.supportedType());
  }

  @Test
  void shouldSuccessfullyUpdateHolding() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_HOLDINGS.getValue()))
      .thenReturn(mappingData);
    when(holdingsStorageClient.getHoldingById(HOLDING_ID)).thenReturn(existingHolding);
    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(holdingsStorageClient).updateHolding(eq(HOLDING_ID), any(HoldingsRecord.class));
    verify(sourceStorageClient).updateSrsRecordGeneration(anyString(), any(Record.class));
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
  }

  @Test
  void shouldHandleError_whenMappingMetadataNotFound() {
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_HOLDINGS.getValue())).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(holdingsStorageClient, never()).getHoldingById(anyString());
    verify(holdingsStorageClient, never()).updateHolding(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains("mapping metadata not found for Holding record with parsedRecordId: " + PARSED_RECORD_ID));
  }

  @Test
  void shouldHandleError_whenHoldingNotFound() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_HOLDINGS.getValue()))
      .thenReturn(mappingData);
    when(holdingsStorageClient.getHoldingById(HOLDING_ID)).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(holdingsStorageClient, never()).updateHolding(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("Holding record with id %s was not found", HOLDING_ID)));
  }

  @Test
  void shouldHandleError_whenSrsRecordNotFound() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_HOLDINGS.getValue()))
      .thenReturn(mappingData);
    when(holdingsStorageClient.getHoldingById(HOLDING_ID)).thenReturn(existingHolding);
    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(holdingsStorageClient).updateHolding(eq(HOLDING_ID), any(HoldingsRecord.class));
    verify(sourceStorageClient, never()).updateSrsRecordGeneration(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("existing SRS record not found for parsedRecordId: %s", PARSED_RECORD_ID)));
  }

  @Test
  void shouldHandleExceptionWhenMappingMetadataRetrievalFails() {
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_HOLDINGS.getValue()))
      .thenThrow(new RuntimeException("Unexpected error"));

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(holdingsStorageClient, never()).updateHolding(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("Error updating holding record for parsedRecordId: %s", PARSED_RECORD_ID)));
  }

  private ParsedRecordDto createParsedRecordDto() {
    var dto = new ParsedRecordDto();
    dto.setId(UUID.fromString(PARSED_RECORD_ID));
    dto.setRecordType(RecordTypeEnum.HOLDING);

    var idsHolder = new ExternalIdsHolder();
    idsHolder.setHoldingsId(UUID.fromString(HOLDING_ID));
    dto.setExternalIdsHolder(idsHolder);

    var parsedRecord = new org.folio.qm.client.model.ParsedRecord();
    parsedRecord.setContent(CONTENT);
    dto.setParsedRecord(parsedRecord);

    var additionalInfo = new org.folio.qm.client.model.AdditionalInfo();
    additionalInfo.setSuppressDiscovery(false);
    dto.setAdditionalInfo(additionalInfo);

    return dto;
  }

  private HoldingsRecord createHolding() {
    var holding = new HoldingsRecord();
    holding.setId(HOLDING_ID);
    holding.setCallNumber(CALL_NUMBER);
    holding.setVersion(1);
    return holding;
  }

  private Record createRecord() {
    var srsRecord = new Record();
    srsRecord.setId(PARSED_RECORD_ID);
    srsRecord.setSnapshotId(SNAPSHOT_ID);
    srsRecord.setRecordType(Record.RecordType.MARC_HOLDING);
    srsRecord.setGeneration(0);
    srsRecord.setOrder(1);
    srsRecord.setMetadata(new Metadata());

    var parsedRecord = new ParsedRecord();
    parsedRecord.setId(PARSED_RECORD_ID);
    parsedRecord.setContent(CONTENT);
    srsRecord.setParsedRecord(parsedRecord);

    return srsRecord;
  }
}
