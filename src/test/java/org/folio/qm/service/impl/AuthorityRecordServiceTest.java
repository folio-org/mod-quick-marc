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
import org.folio.Authority;
import org.folio.Metadata;
import org.folio.ParsedRecord;
import org.folio.Record;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.qm.client.AuthorityStorageClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.ExternalIdsHolder;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.mapper.AuthorityRecordMapper;
import org.folio.qm.mapper.ExternalIdsHolderMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
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
class AuthorityRecordServiceTest {

  private static final String AUTHORITY_ID = UUID.randomUUID().toString();
  private static final String PARSED_RECORD_ID = UUID.randomUUID().toString();
  private static final String SOURCE_FILE_ID = UUID.randomUUID().toString();
  private static final String CONTENT = "{\"leader\":\"00000nz  a2200000n  4500\"}";
  private static final String PERSONAL_NAME = "Personal Name";
  private static final String SNAPSHOT_ID = UUID.randomUUID().toString();

  @Mock
  private MappingMetadataProvider mappingMetadataProvider;
  @Mock
  private SourceStorageClient sourceStorageClient;
  @Mock
  private ExternalIdsHolderMapper externalIdsHolderMapper;
  @Mock
  private AuthorityStorageClient authorityStorageClient;
  @Mock
  private AuthorityRecordMapper mapper;

  @InjectMocks
  private AuthorityRecordService service;

  private ParsedRecordDto parsedRecordDto;
  private DeferredResult<ResponseEntity<Void>> updateResult;
  private Authority existingAuthority;

  @BeforeEach
  void setUp() {
    lenient().when(mappingMetadataProvider.getMappingData(String.valueOf(isA(MappingRecordTypeEnum.class))))
      .thenReturn(new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters()));
    updateResult = new DeferredResult<>();
    parsedRecordDto = createParsedRecordDto();
    existingAuthority = createAuthority();
  }

  @Test
  void supportedType_shouldReturnAuthority() {
    assertEquals(RecordTypeEnum.AUTHORITY, service.supportedType());
  }

  @Test
  void update_shouldSuccessfullyUpdateAuthority_positive() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_AUTHORITY.getValue()))
      .thenReturn(mappingData);
    when(authorityStorageClient.getAuthorityById(AUTHORITY_ID)).thenReturn(existingAuthority);
    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(createRecord());

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(authorityStorageClient).updateAuthority(eq(AUTHORITY_ID), any(Authority.class));
    verify(sourceStorageClient).updateSrsRecordGeneration(anyString(), any(Record.class));
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
  }

  @Test
  void update_shouldHandleError_whenMappingMetadataNotFound_negative() {
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_AUTHORITY.getValue())).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(authorityStorageClient, never()).getAuthorityById(anyString());
    verify(authorityStorageClient, never()).updateAuthority(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains("mapping metadata not found for Authority record with parsedRecordId"));
  }

  @Test
  void update_shouldHandleError_whenAuthorityNotFound_negative() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_AUTHORITY.getValue()))
      .thenReturn(mappingData);
    when(authorityStorageClient.getAuthorityById(AUTHORITY_ID)).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(authorityStorageClient, never()).updateAuthority(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("Authority record with id: %s not found", AUTHORITY_ID)));
  }

  @Test
  void update_shouldHandleError_whenSrsRecordNotFound_negative() {
    var mappingData = new MappingMetadataProvider.MappingData(new JsonObject(), new MappingParameters());
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_AUTHORITY.getValue()))
      .thenReturn(mappingData);
    when(authorityStorageClient.getAuthorityById(AUTHORITY_ID)).thenReturn(existingAuthority);
    when(sourceStorageClient.getSrsRecord(PARSED_RECORD_ID)).thenReturn(null);

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(authorityStorageClient).updateAuthority(eq(AUTHORITY_ID), any(Authority.class));
    verify(sourceStorageClient, never()).updateSrsRecordGeneration(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("existing SRS record not found for parsedRecordId: %s", PARSED_RECORD_ID)));
  }

  @Test
  void update_shouldHandleException_negative() {
    when(mappingMetadataProvider.getMappingData(MappingRecordTypeEnum.MARC_AUTHORITY.getValue()))
      .thenThrow(new RuntimeException("Unexpected error"));

    service.update(UUID.fromString(PARSED_RECORD_ID), updateResult, parsedRecordDto);

    verify(authorityStorageClient, never()).updateAuthority(anyString(), any());
    var result = (ResponseEntity<?>) updateResult.getResult();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).toString()
      .contains(String.format("Error updating authority record for parsedRecordId: %s", PARSED_RECORD_ID)));
  }

  private ParsedRecordDto createParsedRecordDto() {
    var dto = new ParsedRecordDto();
    dto.setId(UUID.fromString(PARSED_RECORD_ID));
    dto.setRecordType(RecordTypeEnum.AUTHORITY);

    var idsHolder = new ExternalIdsHolder();
    idsHolder.setAuthorityId(UUID.fromString(AUTHORITY_ID));
    dto.setExternalIdsHolder(idsHolder);

    var parsedRecord = new org.folio.qm.client.model.ParsedRecord();
    parsedRecord.setContent(CONTENT);
    dto.setParsedRecord(parsedRecord);

    var additionalInfo = new org.folio.qm.client.model.AdditionalInfo();
    additionalInfo.setSuppressDiscovery(false);
    dto.setAdditionalInfo(additionalInfo);

    return dto;
  }

  private Authority createAuthority() {
    var authority = new Authority();
    authority.setId(AUTHORITY_ID);
    authority.setPersonalName(PERSONAL_NAME);
    authority.setSourceFileId(SOURCE_FILE_ID);
    authority.setVersion(1);
    authority.setSource(Authority.Source.MARC);
    return authority;
  }

  private Record createRecord() {
    var srsRecord = new Record();
    srsRecord.setId(PARSED_RECORD_ID);
    srsRecord.setSnapshotId(SNAPSHOT_ID);
    srsRecord.setRecordType(Record.RecordType.MARC_AUTHORITY);
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
