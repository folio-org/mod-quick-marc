package org.folio.qm.service.change;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import java.util.Collections;
import java.util.List;
import org.folio.ExternalIdsHolder;
import org.folio.ParsedRecord;
import org.folio.RawRecord;
import org.folio.Record;
import org.folio.Record.RecordType;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.OptimisticLockingException;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.links.LinksService;
import org.folio.qm.service.mapping.MarcMappingService;
import org.folio.qm.service.population.DefaultValuesPopulationService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.ValidationResult;
import org.folio.qm.service.validation.ValidationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.MarcFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InstanceChangeRecordServiceTest {

  @Mock
  private ValidationService validationService;
  @Mock
  private RecordConversionService conversionService;
  @Mock
  private SourceRecordService sourceRecordService;
  @Mock
  private MarcMappingService<InstanceRecord> mappingService;
  @Mock
  private FolioRecordService<InstanceRecord> folioRecordService;
  @Mock
  private LinksService linksService;
  @Mock
  private DefaultValuesPopulationService defaultValuesPopulationService;

  private InstanceChangeRecordService service;

  @BeforeEach
  void setUp() {
    service = new InstanceChangeRecordService(
      validationService,
      conversionService,
      sourceRecordService,
      mappingService,
      folioRecordService,
      linksService,
      defaultValuesPopulationService
    );
  }

  @Test
  void shouldReturnBibliographicSupportedType() {
    var result = service.supportedType();

    assertEquals(MarcFormat.BIBLIOGRAPHIC, result);
  }

  @Test
  @SuppressWarnings("checkstyle:methodLength")
  void shouldUpdateRecordSuccessfully() {
    var recordId = randomUUID();
    var quickMarcEdit = new QuickMarcEdit();
    quickMarcEdit.setParsedRecordId(recordId);
    quickMarcEdit.setParsedRecordDtoId(randomUUID());
    quickMarcEdit.setExternalId(randomUUID());
    quickMarcEdit.setSourceVersion(1);

    mockDefaultValuesPopulation(quickMarcEdit);
    mockSuccessfulValidation(quickMarcEdit);

    var quickMarcRecord = createQuickMarcRecord();
    quickMarcRecord.setParsedRecordId(recordId);
    quickMarcRecord.setParsedRecordDtoId(quickMarcEdit.getParsedRecordDtoId());
    quickMarcRecord.setExternalId(quickMarcEdit.getExternalId());
    quickMarcRecord.setSourceVersion(1);
    when(conversionService.convert(quickMarcEdit, QuickMarcRecord.class)).thenReturn(quickMarcRecord);

    var existingSourceRecord = createSourceRecord();
    existingSourceRecord.setGeneration(1);
    when(sourceRecordService.get(recordId)).thenReturn(existingSourceRecord);

    var instanceRecord = createInstanceRecord();
    when(folioRecordService.get(quickMarcEdit.getExternalId())).thenReturn(instanceRecord);
    when(mappingService.mapUpdatedRecord(quickMarcRecord, instanceRecord)).thenReturn(instanceRecord);

    service.update(recordId, quickMarcEdit);

    verify(defaultValuesPopulationService).populate(quickMarcEdit);
    verify(validationService).validateMarcRecord(any(BaseQuickMarcRecord.class), eq(Collections.emptyList()));
    verify(validationService).validate(quickMarcEdit);
    verify(sourceRecordService).update(eq(recordId), any(Record.class));
    verify(folioRecordService).update(quickMarcEdit.getExternalId(), instanceRecord);
    verify(linksService).updateRecordLinks(quickMarcRecord);
  }

  @Test
  void shouldThrowValidationExceptionWhenIdsDoNotMatch() {
    var recordId = randomUUID();
    var differentId = randomUUID();
    var quickMarcEdit = new QuickMarcEdit();
    quickMarcEdit.setParsedRecordId(differentId);

    mockDefaultValuesPopulation(quickMarcEdit);

    assertThrows(ValidationException.class, () -> service.update(recordId, quickMarcEdit));

    verify(sourceRecordService, never()).update(any(), any());
    verify(folioRecordService, never()).update(any(), any());
  }

  @Test
  void shouldThrowOptimisticLockingExceptionWhenVersionMismatch() {
    var recordId = randomUUID();
    var quickMarcEdit = new QuickMarcEdit();
    quickMarcEdit.setParsedRecordId(recordId);
    quickMarcEdit.setParsedRecordDtoId(randomUUID());
    quickMarcEdit.setExternalId(randomUUID());
    quickMarcEdit.setSourceVersion(1);

    mockDefaultValuesPopulation(quickMarcEdit);
    mockSuccessfulValidation(quickMarcEdit);

    var quickMarcRecord = createQuickMarcRecord();
    quickMarcRecord.setParsedRecordId(recordId);
    quickMarcRecord.setSourceVersion(1);
    when(conversionService.convert(quickMarcEdit, QuickMarcRecord.class)).thenReturn(quickMarcRecord);

    var existingSourceRecord = createSourceRecord();
    existingSourceRecord.setGeneration(2);
    when(sourceRecordService.get(recordId)).thenReturn(existingSourceRecord);

    assertThrows(OptimisticLockingException.class, () -> service.update(recordId, quickMarcEdit));

    verify(sourceRecordService, never()).update(any(), any());
    verify(folioRecordService, never()).update(any(), any());
    verify(linksService, never()).updateRecordLinks(any());
  }

  @Test
  void shouldThrowFieldsValidationExceptionWhenValidationFails() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);

    var validationResult = new ValidationResult(false, Collections.emptyList());

    mockDefaultValuesPopulation(quickMarcCreate);
    doNothing().when(validationService).validateMarcRecord(any(BaseQuickMarcRecord.class), anyList());
    when(validationService.validate(quickMarcCreate)).thenReturn(validationResult);

    assertThrows(FieldsValidationException.class, () -> service.create(quickMarcCreate));

    verify(folioRecordService, never()).create(any());
    verify(sourceRecordService, never()).create(any());
  }

  @Test
  void shouldCreateRecordSuccessfully() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);

    mockDefaultValuesPopulation(quickMarcCreate);
    mockSuccessfulValidation(quickMarcCreate);
    var quickMarcRecord = createQuickMarcRecordForCreate();
    when(conversionService.convert(quickMarcCreate, QuickMarcRecord.class)).thenReturn(quickMarcRecord);
    var instanceRecord = createInstanceRecord();
    when(mappingService.mapNewRecord(quickMarcRecord)).thenReturn(instanceRecord);
    when(folioRecordService.create(instanceRecord)).thenReturn(instanceRecord);
    var createdSourceRecord = createSourceRecord();
    when(sourceRecordService.create(any(Record.class))).thenReturn(createdSourceRecord);
    var quickMarcView = new QuickMarcView();
    when(conversionService.convert(quickMarcRecord, QuickMarcView.class)).thenReturn(quickMarcView);

    var result = service.create(quickMarcCreate);

    assertNotNull(result);
    assertEquals(quickMarcView, result);
    verify(defaultValuesPopulationService).populate(quickMarcCreate);
    verify(validationService).validateMarcRecord(any(BaseQuickMarcRecord.class), anyList());
    verify(validationService).validate(quickMarcCreate);
    verify(folioRecordService).create(instanceRecord);
    verify(sourceRecordService).create(any(Record.class));
  }

  @Test
  void shouldCreateFolioRecordFirst() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);

    mockDefaultValuesPopulation(quickMarcCreate);
    mockSuccessfulValidation(quickMarcCreate);
    var quickMarcRecord = createQuickMarcRecordForCreate();
    when(conversionService.convert(quickMarcCreate, QuickMarcRecord.class)).thenReturn(quickMarcRecord);
    var instanceRecord = createInstanceRecord();
    when(mappingService.mapNewRecord(quickMarcRecord)).thenReturn(instanceRecord);
    when(folioRecordService.create(instanceRecord)).thenReturn(instanceRecord);
    var createdSourceRecord = createSourceRecord();
    when(sourceRecordService.create(any(Record.class))).thenReturn(createdSourceRecord);
    when(conversionService.convert(any(), eq(QuickMarcView.class))).thenReturn(new QuickMarcView());

    service.create(quickMarcCreate);

    verify(folioRecordService).create(instanceRecord);
    verify(sourceRecordService).create(any(Record.class));
  }

  @Test
  void shouldPopulateDefaultValuesOnCreate() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);

    mockDefaultValuesPopulation(quickMarcCreate);
    mockSuccessfulValidation(quickMarcCreate);

    var quickMarcRecord = createQuickMarcRecordForCreate();
    when(conversionService.convert(quickMarcCreate, QuickMarcRecord.class)).thenReturn(quickMarcRecord);

    var instanceRecord = createInstanceRecord();
    when(mappingService.mapNewRecord(any())).thenReturn(instanceRecord);
    when(folioRecordService.create(any())).thenReturn(instanceRecord);
    when(sourceRecordService.create(any())).thenReturn(createSourceRecord());
    when(conversionService.convert(any(), eq(QuickMarcView.class))).thenReturn(new QuickMarcView());

    service.create(quickMarcCreate);

    verify(defaultValuesPopulationService).populate(quickMarcCreate);
  }

  @Test
  void shouldSetExternalIdsInQuickMarcRecordAfterFolioRecordCreation() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);

    mockDefaultValuesPopulation(quickMarcCreate);
    mockSuccessfulValidation(quickMarcCreate);

    var quickMarcRecord = createQuickMarcRecordForCreate();
    when(conversionService.convert(quickMarcCreate, QuickMarcRecord.class)).thenReturn(quickMarcRecord);

    var instanceId = randomUUID();
    var instanceHrid = "in00000123";
    var instanceRecord = createInstanceRecord();
    instanceRecord.setId(instanceId.toString());
    instanceRecord.setHrid(instanceHrid);
    when(mappingService.mapNewRecord(quickMarcRecord)).thenReturn(instanceRecord);
    when(folioRecordService.create(instanceRecord)).thenReturn(instanceRecord);

    when(sourceRecordService.create(any(Record.class))).thenReturn(createSourceRecord());
    when(conversionService.convert(any(), eq(QuickMarcView.class))).thenReturn(new QuickMarcView());

    service.create(quickMarcCreate);

    assertEquals(instanceId, quickMarcRecord.getExternalId());
    assertEquals(instanceHrid, quickMarcRecord.getExternalHrid());
  }

  @Test
  void shouldValidateMarcRecordBeforeCreate() {
    var quickMarcCreate = new QuickMarcCreate();
    quickMarcCreate.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);

    mockDefaultValuesPopulation(quickMarcCreate);
    doNothing().when(validationService).validateMarcRecord(any(BaseQuickMarcRecord.class), anyList());
    when(validationService.validate(quickMarcCreate))
      .thenReturn(new ValidationResult(false, Collections.emptyList()));

    assertThrows(FieldsValidationException.class, () -> service.create(quickMarcCreate));

    verify(validationService).validateMarcRecord(any(BaseQuickMarcRecord.class), anyList());
    verify(validationService).validate(quickMarcCreate);
    verify(folioRecordService, never()).create(any());
    verify(sourceRecordService, never()).create(any());
  }

  private void mockDefaultValuesPopulation(BaseQuickMarcRecord quickMarcRecord) {
    doNothing().when(defaultValuesPopulationService).populate(quickMarcRecord);
  }

  private void mockSuccessfulValidation(BaseQuickMarcRecord quickMarcRecord) {
    doNothing().when(validationService).validateMarcRecord(any(BaseQuickMarcRecord.class), anyList());
    when(validationService.validate(quickMarcRecord))
      .thenReturn(new ValidationResult(true, Collections.emptyList()));
  }

  private QuickMarcRecord createQuickMarcRecordForCreate() {
    var factory = MarcFactory.newInstance();
    var marcRecord = factory.newRecord();
    marcRecord.setLeader(factory.newLeader("00000nam a2200000 a 4500"));

    var parsedContent = new JsonObject();
    parsedContent.put("leader", "00000nam a2200000 a 4500");
    parsedContent.put("fields", new io.vertx.core.json.JsonArray());

    var baseMarcRecord = new QuickMarcCreate();
    baseMarcRecord.setLeader("00000nam a2200000 a 4500");
    baseMarcRecord.setFields(List.of(new FieldItem().tag("245").content("Test")));
    baseMarcRecord.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);

    return QuickMarcRecord.builder()
      .source(baseMarcRecord)
      .marcRecord(marcRecord)
      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .parsedContent(parsedContent)
      .suppressDiscovery(false)
      .build();
  }

  private QuickMarcRecord createQuickMarcRecord() {
    var factory = MarcFactory.newInstance();
    var marcRecord = factory.newRecord();
    marcRecord.setLeader(factory.newLeader("00000nam a2200000 a 4500"));

    var parsedContent = new JsonObject();
    parsedContent.put("leader", "00000nam a2200000 a 4500");
    parsedContent.put("fields", new io.vertx.core.json.JsonArray());

    var baseMarcRecord = new QuickMarcEdit();
    baseMarcRecord.setLeader("00000nam a2200000 a 4500");
    baseMarcRecord.setFields(List.of(new FieldItem().tag("245").content("Test")));

    return QuickMarcRecord.builder()
      .source(baseMarcRecord)
      .marcRecord(marcRecord)
      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .parsedContent(parsedContent)
      .suppressDiscovery(false)
      .externalId(randomUUID())
      .externalHrid("in00000001")
      .parsedRecordId(randomUUID())
      .parsedRecordDtoId(randomUUID())
      .build();
  }

  private InstanceRecord createInstanceRecord() {
    var instanceRecord = new InstanceRecord();
    instanceRecord.setId(randomUUID().toString());
    instanceRecord.setHrid("in00000001");
    return instanceRecord;
  }

  private Record createSourceRecord() {
    var recordId = randomUUID().toString();
    var parsedRecordId = randomUUID().toString();

    return new Record()
      .withId(recordId)
      .withRecordType(RecordType.MARC_BIB)
      .withGeneration(0)
      .withRawRecord(new RawRecord().withId(recordId))
      .withExternalIdsHolder(new ExternalIdsHolder())
      .withParsedRecord(new ParsedRecord()
        .withId(parsedRecordId)
        .withContent(new JsonObject()));
  }
}
