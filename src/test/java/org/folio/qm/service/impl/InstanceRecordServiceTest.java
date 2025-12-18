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
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.ExternalIdsHolder;
import org.folio.qm.client.model.Instance;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.PrecedingSucceedingTitleCollection;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.mapper.ExternalIdsHolderMapper;
import org.folio.qm.mapper.InstanceRecordMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
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
class InstanceRecordServiceTest {

  private static final String INSTANCE_ID = UUID.randomUUID().toString();
  private static final String PARSED_RECORD_ID = UUID.randomUUID().toString();
  private static final String CONTENT = "{\"leader\":\"00000nam  a2200000n  4500\"}";
  private static final String TITLE = "Test Instance Title";
  private static final String SNAPSHOT_ID = UUID.randomUUID().toString();

  @Mock
  private MappingMetadataProvider mappingMetadataProvider;
  @Mock
  private SourceStorageClient sourceStorageClient;
  @Mock
  private ExternalIdsHolderMapper externalIdsHolderMapper;
  @Mock
  private InstanceStorageClient instanceStorageClient;
  @Mock
  private InstanceRecordMapper mapper;
  @Mock
  private PrecedingSucceedingTitlesHelper precedingSucceedingTitlesHelper;
  @Mock
  private PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;

  @InjectMocks
  private InstanceRecordService service;

  private ParsedRecordDto parsedRecordDto;
  private DeferredResult<ResponseEntity<Void>> updateResult;
  private Instance existingInstance;
  private PrecedingSucceedingTitleCollection titleCollection;

  @BeforeEach
  void setUp() {
    lenient().when(mappingMetadataProvider.getMappingData(String.valueOf(isA(MappingRecordTypeEnum.class))))
      .thenReturn(new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters()));
    lenient().when(precedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(any(Instance.class)))
      .thenReturn(titleCollection);
    lenient().doNothing().when(precedingSucceedingTitlesClient).updateTitles(anyString(),
      any(PrecedingSucceedingTitleCollection.class));

    titleCollection = new PrecedingSucceedingTitleCollection();
    updateResult = new DeferredResult<>();
    parsedRecordDto = createParsedRecordDto();
    existingInstance = createInstance();
  }

  @Test
  void supportedType_shouldReturnBib() {
    assertEquals(RecordTypeEnum.BIB, service.supportedType());
  }

  @Test
  void update_shouldSuccessfullyUpdateInstance_positive() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_BIB.getValue()))
      .thenReturn(mappingData);
    when(instanceStorageClient.getInstanceById(INSTANCE_ID)).thenReturn(existingInstance);
    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(instanceStorageClient).updateInstance(eq(INSTANCE_ID), any(Instance.class));
    verify(sourceStorageClient).updateSrsRecordGeneration(anyString(), any(Record.class));
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
  }

  @Test
  void update_shouldHandleError_whenMappingMetadataNotFound_negative() {
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_BIB.getValue())).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(instanceStorageClient, never()).getInstanceById(anyString());
    verify(instanceStorageClient, never()).updateInstance(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString().contains(
      String.format("mapping metadata not found for MARC-BIB record with parsedRecordId: %s", PARSED_RECORD_ID)));
  }

  @Test
  void update_shouldHandleError_whenInstanceNotFound_negative() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_BIB.getValue()))
      .thenReturn(mappingData);
    when(instanceStorageClient.getInstanceById(INSTANCE_ID)).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(instanceStorageClient, never()).updateInstance(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("Instance record with id: %s not found", INSTANCE_ID)));
  }

  @Test
  void update_shouldHandleError_whenSrsRecordNotFound_negative() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_BIB.getValue()))
      .thenReturn(mappingData);
    when(instanceStorageClient.getInstanceById(INSTANCE_ID)).thenReturn(existingInstance);
    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(instanceStorageClient).updateInstance(eq(INSTANCE_ID), any(Instance.class));
    verify(sourceStorageClient, never()).updateSrsRecordGeneration(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("existing SRS record not found for parsedRecordId: %s", PARSED_RECORD_ID)));
  }

  @Test
  void update_shouldHandleException_negative() {
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_BIB.getValue()))
      .thenThrow(new RuntimeException("Unexpected error"));

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(instanceStorageClient, never()).updateInstance(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("Error updating Instance record for parsedRecordId: %s", PARSED_RECORD_ID)));
  }

  private ParsedRecordDto createParsedRecordDto() {
    var dto = new ParsedRecordDto();
    dto.setId(UUID.fromString(PARSED_RECORD_ID));
    dto.setRecordType(RecordTypeEnum.BIB);

    var idsHolder = new ExternalIdsHolder();
    idsHolder.setInstanceId(UUID.fromString(INSTANCE_ID));
    dto.setExternalIdsHolder(idsHolder);

    var parsedRecord = new org.folio.qm.client.model.ParsedRecord();
    parsedRecord.setContent(CONTENT);
    dto.setParsedRecord(parsedRecord);

    var additionalInfo = new org.folio.qm.client.model.AdditionalInfo();
    additionalInfo.setSuppressDiscovery(false);
    dto.setAdditionalInfo(additionalInfo);

    return dto;
  }

  private Instance createInstance() {
    var instance = new Instance();
    instance.setId(INSTANCE_ID);
    instance.setTitle(TITLE);
    instance.setVersion(1);
    instance.setSource("MARC");
    return instance;
  }

  private Record createRecord() {
    var srsRecord = new Record();
    srsRecord.setId(PARSED_RECORD_ID);
    srsRecord.setSnapshotId(SNAPSHOT_ID);
    srsRecord.setRecordType(Record.RecordType.MARC_BIB);
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
