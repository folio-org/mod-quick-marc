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
//import org.folio.qm.client.InstanceStorageClient;
//import org.folio.qm.client.PrecedingSucceedingTitlesClient;
//import org.folio.qm.client.SourceStorageClient;
//import org.folio.qm.client.model.Instance;
//import org.folio.qm.domain.model.MappingRecordType;
//import org.folio.qm.client.model.RecordTypeEnum;
//import org.folio.qm.converter.MarcQmConverter;
//import org.folio.qm.domain.dto.MarcFormat;
//import org.folio.qm.domain.dto.QuickMarcEdit;
//import org.folio.qm.exception.MappingMetadataException;
//import org.folio.qm.mapper.InstanceRecordMapper;
//import org.folio.qm.mapper.MarcTypeMapper;
//import org.folio.qm.service.support.MappingMetadataProvider;
//import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
//import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitles;
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
//class InstanceRecordServiceTest {
//
//  private static final String INSTANCE_ID = UUID.randomUUID().toString();
//  private static final String PARSED_RECORD_ID = UUID.randomUUID().toString();
//  private static final String CONTENT = "{\"leader\":\"00000nam  a2200000n  4500\"}";
//  private static final String TITLE = "Test Instance Title";
//  private static final String SNAPSHOT_ID = UUID.randomUUID().toString();
//  private final UUID parsedRecordId = UUID.fromString(PARSED_RECORD_ID);
//
//  @Mock
//  private MappingMetadataProvider mappingMetadataProvider;
//  @Mock
//  private SourceStorageClient sourceStorageClient;
//  @Mock
//  private InstanceStorageClient instanceStorageClient;
//  @Mock
//  private InstanceRecordMapper mapper;
//  @Mock
//  private PrecedingSucceedingTitlesHelper precedingSucceedingTitlesHelper;
//  @Mock
//  private PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;
//  @Mock
//  private FolioExecutionContext folioExecutionContext;
//  @Mock
//  private MarcQmConverter<QuickMarcEdit> marcQmConverter;
//  @Mock
//  private MarcTypeMapper typeMapper;
//
//  @InjectMocks
//  private InstanceRecordService service;
//
//  private QuickMarcEdit quickMarc;
//  private Instance existingInstance;
//
//  @BeforeEach
//  void setUp() {
//    lenient().when(mappingMetadataProvider.getMappingData(isA(String.class)))
//      .thenReturn(new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters()));
//    var titleCollection = new InstancePrecedingSucceedingTitles();
//    lenient().when(precedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(any(Instance.class)))
//      .thenReturn(titleCollection);
//    lenient().doNothing().when(precedingSucceedingTitlesClient).updateTitles(anyString(),
//      any(InstancePrecedingSucceedingTitles.class));
//    lenient().when(folioExecutionContext.getUserId()).thenReturn(UUID.randomUUID());
//    var objectMapper = new ObjectMapper();
//    ObjectNode parsedNode = objectMapper.createObjectNode();
//    parsedNode.put("leader", "00000nam  a2200000n  4500");
//    lenient().when(marcQmConverter.convertToParsedContent(any(QuickMarcEdit.class))).thenReturn(parsedNode);
//    lenient().when(typeMapper.toDto(any(MarcFormat.class))).thenReturn(RecordTypeEnum.BIB);
//
//    quickMarc = createQuickMarc();
//    existingInstance = createInstance();
//  }
//
//  @Test
//  void shouldReturnBibSupportedType() {
//    assertEquals(MarcFormat.BIBLIOGRAPHIC, service.supportedType());
//  }
//
//  @Test
//  void shouldSuccessfullyUpdateInstance() {
//    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_BIB.getValue()))
//      .thenReturn(mappingData);
//    when(instanceStorageClient.getInstanceById(INSTANCE_ID)).thenReturn(existingInstance);
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//
//    service.update(quickMarc);
//
//    verify(instanceStorageClient).updateInstance(eq(INSTANCE_ID), any(Instance.class));
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//  }
//
//  @Test
//  void shouldThrowMappingMetadataException_whenMappingMetadataNotFound() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_BIB.getValue())).thenReturn(null);
//
//    var exception = assertThrows(MappingMetadataException.class, () ->
//      service.update(quickMarc)
//    );
//
//    assertEquals(String.format("mapping metadata not found for %s record with parsedRecordId: %s",
//      MappingRecordType.MARC_BIB.getValue(), parsedRecordId), exception.getMessage());
//    verify(instanceStorageClient, never()).getInstanceById(anyString());
//    verify(instanceStorageClient, never()).updateInstance(anyString(), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//  }
//
//  @Test
//  void shouldThrowNotFoundException_whenInstanceNotFound() {
//    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_BIB.getValue()))
//      .thenReturn(mappingData);
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    when(instanceStorageClient.getInstanceById(INSTANCE_ID)).thenReturn(null);
//
//    var exception = assertThrows(NotFoundException.class, () ->
//      service.update(quickMarc)
//    );
//
//    verify(instanceStorageClient, never()).updateInstance(anyString(), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//    assertEquals(String.format("Instance record with id: %s not found", INSTANCE_ID), exception.getMessage());
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
//    verify(instanceStorageClient, never()).updateInstance(eq(INSTANCE_ID), any(Instance.class));
//    verify(sourceStorageClient, never()).updateSrsRecord(anyString(), any());
//    assertEquals(String.format("The SRS record to update was not found for parsedRecordId: %s", parsedRecordId),
//      exception.getMessage());
//  }
//
//  @Test
//  void shouldThrowRuntimeException_whenMappingMetadataRetrievalFails() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_BIB.getValue()))
//      .thenThrow(new RuntimeException("Unexpected error"));
//
//    var exception = assertThrows(RuntimeException.class, () ->
//      service.update(quickMarc)
//    );
//
//    assertEquals(String.format("Error mapping %s record with parsedRecordId: %s",
//      MappingRecordType.MARC_BIB.getValue(), parsedRecordId), exception.getMessage());
//    verify(mappingMetadataProvider).getMappingData(MappingRecordType.MARC_BIB.getValue());
//    verify(instanceStorageClient, never()).getInstanceById(anyString());
//    verify(instanceStorageClient, never()).updateInstance(anyString(), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any());
//  }
//
//  private QuickMarcEdit createQuickMarc() {
//    var quick = new QuickMarcEdit();
//    quick.setParsedRecordDtoId(UUID.fromString(PARSED_RECORD_ID));
//    quick.setParsedRecordId(UUID.fromString(PARSED_RECORD_ID));
//    quick.setExternalId(UUID.fromString(INSTANCE_ID));
//    quick.setExternalHrid("hrid-1");
//    quick.setSuppressDiscovery(false);
//    quick.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
//    quick.setLeader("00000nam  a2200000n  4500");
//
//    var field = new org.folio.qm.domain.dto.FieldItem();
//    field.setTag("245");
//    field.setContent("$a Test Title");
//    quick.addFieldsItem(field);
//    return quick;
//  }
//
//  private Instance createInstance() {
//    var instance = new Instance();
//    instance.setId(INSTANCE_ID);
//    instance.setTitle(TITLE);
//    instance.setVersion(1);
//    instance.setSource("MARC");
//    return instance;
//  }
//
//  private Record createRecord() {
//    var srsRecord = new Record();
//    srsRecord.setId(PARSED_RECORD_ID);
//    srsRecord.setSnapshotId(SNAPSHOT_ID);
//    srsRecord.setRecordType(Record.RecordType.MARC_BIB);
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
