package org.folio.qm.service.mapping;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import java.util.List;
import org.folio.Authority;
import org.folio.processing.mapping.defaultmapper.MarcToAuthorityMapper;
import org.folio.processing.mapping.defaultmapper.MarkToAuthorityExtendedMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.MappingMetadataException;
import org.folio.qm.service.storage.config.AuthoritiesConfigService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.service.support.MappingMetadataProvider.MappingData;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcMappingAuthorityServiceTest {

  private @Mock MappingMetadataProvider mappingMetadataProvider;
  private @Mock FolioRecordMerger<AuthorityRecord, Authority> merger;
  private @Mock AuthoritiesConfigService authoritiesConfigService;
  private @Spy RecordMapper<Authority> simpleMapper = new MarcToAuthorityMapper();
  private @Spy RecordMapper<Authority> extendedMapper = new MarkToAuthorityExtendedMapper();

  private MarcMappingAuthorityService service;

  @BeforeEach
  void setUp() {
    service = new MarcMappingAuthorityService(mappingMetadataProvider, merger, authoritiesConfigService,
      List.of(simpleMapper, extendedMapper));
  }

  @Test
  void shouldMapNewRecord() {
    var qmRecord = createQuickMarcRecord();
    var mappingData = createMappingData();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY))
      .thenReturn(mappingData);

    var result = service.mapNewRecord(qmRecord);

    assertNotNull(result);
    assertEquals(Authority.Source.MARC, result.getSource());
    verify(mappingMetadataProvider).getMappingData(MappingRecordType.MARC_AUTHORITY);
    verify(merger).merge(any(), any());
  }

  @Test
  void shouldMapUpdatedRecord() {
    var qmRecord = createQuickMarcRecord();
    var existingRecord = new AuthorityRecord();
    existingRecord.setId("existing-id");
    var mappingData = createMappingData();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY))
      .thenReturn(mappingData);

    var result = service.mapUpdatedRecord(qmRecord, existingRecord);

    assertNotNull(result);
    assertEquals("existing-id", result.getId());
    assertEquals(Authority.Source.MARC, result.getSource());
    verify(mappingMetadataProvider).getMappingData(MappingRecordType.MARC_AUTHORITY);
    verify(merger).merge(any(), any());
  }

  @Test
  void shouldThrowExceptionWhenMappingMetadataNotFound() {
    var qmRecord = createQuickMarcRecord();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY))
      .thenReturn(null);

    var exception = assertThrows(MappingMetadataException.class,
      () -> service.mapNewRecord(qmRecord));

    assertNotNull(exception);
    assertNotNull(exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenMappingFails() {
    var qmRecord = createQuickMarcRecord();

    when(mappingMetadataProvider.getMappingData(MappingRecordType.MARC_AUTHORITY))
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
  void shouldReturnSimpleAuthorityRecordMapper() {
    when(authoritiesConfigService.isAuthorityExtendedMappingEnabled()).thenReturn(false);

    var mapper = service.getRecordMapper();

    assertEquals(simpleMapper, mapper);
  }

  @Test
  void shouldReturnExtendedAuthorityRecordMapper() {
    when(authoritiesConfigService.isAuthorityExtendedMappingEnabled()).thenReturn(true);

    var mapper = service.getRecordMapper();

    assertEquals(extendedMapper, mapper);
  }

  private QuickMarcRecord createQuickMarcRecord() {
    var qmRecord = new QuickMarcRecord();
    qmRecord.setMappingRecordType(MappingRecordType.MARC_AUTHORITY);
    qmRecord.setParsedRecordId(randomUUID());
    qmRecord.setParsedContent(new JsonObject().put("leader", "00000naa a2200000"));
    return qmRecord;
  }

  private MappingData createMappingData() {
    var mappingRules = new JsonObject().put("001", new JsonObject());
    var mappingParameters = new MappingParameters();
    return new MappingData(mappingRules, mappingParameters);
  }
}
