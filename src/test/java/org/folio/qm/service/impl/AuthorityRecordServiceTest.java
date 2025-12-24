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
//import org.folio.Authority;
//import org.folio.Metadata;
//import org.folio.ParsedRecord;
//import org.folio.RawRecord;
//import org.folio.Record;
//import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
//import org.folio.qm.client.AuthorityStorageClient;
//import org.folio.qm.client.SourceStorageClient;
//import org.folio.qm.client.model.MappingRecordType;
//import org.folio.qm.client.model.RecordTypeEnum;
//import org.folio.qm.converter.MarcQmConverter;
//import org.folio.qm.domain.dto.MarcFormat;
//import org.folio.qm.domain.dto.QuickMarcEdit;
//import org.folio.qm.exception.MappingMetadataException;
//import org.folio.qm.mapper.AuthorityRecordMapper;
//import org.folio.qm.mapper.MarcTypeMapper;
//import org.folio.qm.service.support.MappingMetadataProvider;
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
//class AuthorityRecordServiceTest {
//
//  private static final String AUTHORITY_ID = UUID.randomUUID().toString();
//  private static final String PARSED_RECORD_ID = UUID.randomUUID().toString();
//  private static final String SOURCE_FILE_ID = UUID.randomUUID().toString();
//  private static final String CONTENT = "{\"leader\":\"00000nz  a2200000n  4500\"}";
//  private static final String PERSONAL_NAME = "Personal Name";
//  private static final String SNAPSHOT_ID = UUID.randomUUID().toString();
//  private final UUID parsedRecordId = UUID.fromString(PARSED_RECORD_ID);
//
//  @Mock
//  private MappingMetadataProvider mappingMetadataProvider;
//  @Mock
//  private SourceStorageClient sourceStorageClient;
//  @Mock
//  private AuthorityStorageClient authorityStorageClient;
//  @Mock
//  private AuthorityRecordMapper mapper;
//  @Mock
//  private MarcQmConverter<QuickMarcEdit> marcQmConverter;
//  @Mock
//  private MarcTypeMapper typeMapper;
//  @InjectMocks
//  private AuthorityRecordService service;
//
//  private QuickMarcEdit quickMarc;
//  private Authority existingAuthority;
//
//  @BeforeEach
//  void setUp() {
//    lenient().when(mappingMetadataProvider.getMappingData(isA(String.class)))
//      .thenReturn(new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters()));
//    var objectMapper = new ObjectMapper();
//    ObjectNode parsedNode = objectMapper.createObjectNode();
//    parsedNode.put("leader", "00000nam  a2200000n  4500");
//    lenient().when(marcQmConverter.convertToParsedContent(any(QuickMarcEdit.class))).thenReturn(parsedNode);
//    lenient().when(typeMapper.toDto(any(MarcFormat.class))).thenReturn(RecordTypeEnum.AUTHORITY);
//
//    quickMarc = createQuickMarc();
//    existingAuthority = createAuthority();
//  }
//
//  @Test
//  void supportedType_shouldReturnAuthority() {
//    assertEquals(MarcFormat.AUTHORITY, service.supportedType());
//  }
//
//  @Test
//  void update_shouldSuccessfullyUpdateAuthority_positive() {
//    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY.getValue()))
//      .thenReturn(mappingData);
//    when(authorityStorageClient.getAuthorityById(AUTHORITY_ID)).thenReturn(existingAuthority);
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//
//    service.update(quickMarc);
//
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//    verify(authorityStorageClient).updateAuthority(eq(AUTHORITY_ID), any(Authority.class));
//  }
//
//  @Test
//  void update_shouldThrowMappingMetadataException_whenMappingMetadataNotFound_negative() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY.getValue())).thenReturn(null);
//
//    var exception = assertThrows(MappingMetadataException.class, () ->
//      service.update(quickMarc)
//    );
//
//    assertEquals(String.format("mapping metadata not found for %s record with parsedRecordId: %s",
//      MappingRecordType.MARC_AUTHORITY.getValue(), parsedRecordId), exception.getMessage());
//    verify(authorityStorageClient, never()).getAuthorityById(anyString());
//    verify(authorityStorageClient, never()).updateAuthority(anyString(), any());
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//  }
//
//  @Test
//  void update_shouldThrowNotFoundException_whenAuthorityNotFound_negative() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY.getValue()))
//      .thenReturn(mappingData);
//    when(authorityStorageClient.getAuthorityById(AUTHORITY_ID)).thenReturn(null);
//
//    var exception = assertThrows(NotFoundException.class, () ->
//      service.update(quickMarc)
//    );
//
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//    verify(authorityStorageClient, never()).updateAuthority(anyString(), any());
//    assertEquals(String.format("Authority record with id: %s not found", AUTHORITY_ID), exception.getMessage());
//  }
//
//  @Test
//  void update_shouldThrowNotFoundException_whenSrsRecordNotFound_negative() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(null);
//
//    var exception = assertThrows(NotFoundException.class, () ->
//      service.update(quickMarc)
//    );
//
//    assertEquals(String.format("The SRS record to update was not found for parsedRecordId: %s", parsedRecordId),
//      exception.getMessage());
//    verify(authorityStorageClient, never()).getAuthorityById(AUTHORITY_ID);
//    verify(authorityStorageClient, never()).updateAuthority(eq(AUTHORITY_ID), any(Authority.class));
//    verify(sourceStorageClient, never()).updateSrsRecord(anyString(), any());
//  }
//
//  @Test
//  void update_shouldHandleException_negative() {
//    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());
//    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY.getValue()))
//      .thenThrow(new RuntimeException("Unexpected error"));
//
//    var exception = assertThrows(RuntimeException.class, () ->
//      service.update(quickMarc)
//    );
//    verify(sourceStorageClient).updateSrsRecord(anyString(), any(Record.class));
//    verify(authorityStorageClient, never()).updateAuthority(anyString(), any());
//    assertEquals(String.format("Error mapping %s record with parsedRecordId: %s",
//        MappingRecordType.MARC_AUTHORITY.getValue(), parsedRecordId),
//      exception.getMessage());
//  }
//
//  private QuickMarcEdit createQuickMarc() {
//    var quick = new QuickMarcEdit();
//    quick.setParsedRecordDtoId(UUID.fromString(PARSED_RECORD_ID));
//    quick.setParsedRecordId(UUID.fromString(PARSED_RECORD_ID));
//    quick.setExternalId(UUID.fromString(AUTHORITY_ID));
//    quick.setExternalHrid("hrid-1");
//    quick.setSuppressDiscovery(false);
//    quick.setMarcFormat(MarcFormat.AUTHORITY);
//    quick.setLeader("00000nam  a2200000n  4500");
//
//    var field = new org.folio.qm.domain.dto.FieldItem();
//    field.setTag("245");
//    field.setContent("$a Test Title");
//    quick.addFieldsItem(field);
//    return quick;
//  }
//
//  private Authority createAuthority() {
//    var authority = new Authority();
//    authority.setId(AUTHORITY_ID);
//    authority.setPersonalName(PERSONAL_NAME);
//    authority.setSourceFileId(SOURCE_FILE_ID);
//    authority.setVersion(1);
//    authority.setSource(Authority.Source.MARC);
//    return authority;
//  }
//
//  private Record createRecord() {
//    var srsRecord = new Record();
//    srsRecord.setId(PARSED_RECORD_ID);
//    srsRecord.setSnapshotId(SNAPSHOT_ID);
//    srsRecord.setRecordType(Record.RecordType.MARC_AUTHORITY);
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
