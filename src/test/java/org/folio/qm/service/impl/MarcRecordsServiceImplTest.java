package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.Instance;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.exception.OptimisticLockingException;
import org.folio.qm.service.ChangeManagerService;
import org.folio.qm.service.LinksService;
import org.folio.qm.service.RecordService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.validation.ValidationResult;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

@ExtendWith(MockitoExtension.class)
class MarcRecordsServiceImplTest {

  private static final UUID PARSED_RECORD_ID = UUID.randomUUID();
  private static final UUID EXTERNAL_ID = UUID.randomUUID();
  private static final Integer CURRENT_VERSION = 1;
  private static final Integer OLD_VERSION = 0;

  @Mock
  private DefaultValuesPopulationService populationService;
  @Mock
  private ValidationService validationService;
  @Mock
  private ConversionService conversionService;
  @Mock
  private ChangeManagerService changeManagerService;
  @Mock
  private LinksService linksService;
  @Mock
  private MarcRecordServiceRegistry marcRecordServiceRegistry;
  @Mock
  private RecordService<Instance> recordService;
  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private FolioModuleMetadata folioModuleMetadata;

  @InjectMocks
  private MarcRecordsServiceImpl recordsService;

  @Captor
  private ArgumentCaptor<QuickMarcCreate> marcCaptor;

  private QuickMarcEdit quickMarcEdit;
  private SourceRecord sourceRecord;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    quickMarcEdit = createQuickMarcEdit();
    sourceRecord = createSourceRecord();
    lenient().when(folioExecutionContext.getFolioModuleMetadata()).thenReturn(folioModuleMetadata);
    lenient().when(marcRecordServiceRegistry.get(MarcFormat.BIBLIOGRAPHIC)).thenReturn((RecordService) recordService);
  }

  @Test
  void updateById_shouldSuccessfullyUpdate_positive() {
    when(changeManagerService.getSourceRecordByExternalId(EXTERNAL_ID.toString())).thenReturn(sourceRecord);
    when(validationService.validate(quickMarcEdit)).thenReturn(new ValidationResult(true, Collections.emptyList()));
    doNothing().when(populationService).populate(quickMarcEdit);
    doNothing().when(validationService).validateMarcRecord(any(), any());
    doNothing().when(validationService).validateIdsMatch(quickMarcEdit, PARSED_RECORD_ID);
    doNothing().when(recordService).update(quickMarcEdit);
    doNothing().when(linksService).updateRecordLinks(quickMarcEdit);

    recordsService.updateById(PARSED_RECORD_ID, quickMarcEdit);

    verify(populationService).populate(quickMarcEdit);
    verify(changeManagerService).getSourceRecordByExternalId(EXTERNAL_ID.toString());
    verify(validationService).validateMarcRecord(quickMarcEdit, Collections.emptyList());
    verify(validationService).validateIdsMatch(quickMarcEdit, PARSED_RECORD_ID);
    verify(marcRecordServiceRegistry).get(MarcFormat.BIBLIOGRAPHIC);
    verify(recordService).update(quickMarcEdit);
    verify(linksService).updateRecordLinks(quickMarcEdit);
    verify(conversionService, never()).convert(any(QuickMarcEdit.class), eq(ParsedRecordDto.class));
  }

  @Test
  void updateById_shouldThrowOptimisticLockingException_whenVersionMismatch_negative() {
    quickMarcEdit.setSourceVersion(OLD_VERSION);
    when(changeManagerService.getSourceRecordByExternalId(EXTERNAL_ID.toString())).thenReturn(sourceRecord);

    assertThrows(OptimisticLockingException.class,
      () -> recordsService.updateById(PARSED_RECORD_ID, quickMarcEdit));

    verify(changeManagerService).getSourceRecordByExternalId(EXTERNAL_ID.toString());
    verify(recordService, never()).update(any());
    verify(linksService, never()).updateRecordLinks(any());
  }

  @Test
  void updateById_shouldValidateIdsMatch_positive() {
    when(changeManagerService.getSourceRecordByExternalId(EXTERNAL_ID.toString())).thenReturn(sourceRecord);
    when(validationService.validate(quickMarcEdit)).thenReturn(new ValidationResult(true, Collections.emptyList()));
    doNothing().when(populationService).populate(quickMarcEdit);
    doNothing().when(validationService).validateMarcRecord(any(), any());
    doNothing().when(validationService).validateIdsMatch(quickMarcEdit, PARSED_RECORD_ID);
    doNothing().when(recordService).update(quickMarcEdit);
    doNothing().when(linksService).updateRecordLinks(quickMarcEdit);

    recordsService.updateById(PARSED_RECORD_ID, quickMarcEdit);

    verify(validationService).validateIdsMatch(quickMarcEdit, PARSED_RECORD_ID);
  }

  @Test
  void updateById_shouldCallLinksServiceOnCompletion_positive() {
    when(changeManagerService.getSourceRecordByExternalId(EXTERNAL_ID.toString())).thenReturn(sourceRecord);
    when(validationService.validate(quickMarcEdit)).thenReturn(new ValidationResult(true, Collections.emptyList()));
    doNothing().when(populationService).populate(quickMarcEdit);
    doNothing().when(validationService).validateMarcRecord(any(), any());
    doNothing().when(validationService).validateIdsMatch(quickMarcEdit, PARSED_RECORD_ID);
    doNothing().when(recordService).update(quickMarcEdit);
    doNothing().when(linksService).updateRecordLinks(quickMarcEdit);

    recordsService.updateById(PARSED_RECORD_ID, quickMarcEdit);

    verify(recordService).update(quickMarcEdit);
    verify(linksService).updateRecordLinks(quickMarcEdit);
  }

  @MethodSource("createRecordCleanupTestData")
  @ParameterizedTest
  void createRecord_positive_shouldRemoveFieldsBasedOnRecordType(MarcFormat marcFormat, List<FieldItem> input,
                                                                 List<FieldItem> output) {
    // Arrange
    var marc = new QuickMarcCreate().marcFormat(marcFormat);
    marc.getFields().addAll(input);

    doNothing().when(populationService).populate(marc);
    when(validationService.validate(marc)).thenReturn(new ValidationResult(true, Collections.emptyList()));
    when(conversionService.convert(marcCaptor.capture(), eq(ParsedRecordDto.class))).thenReturn(new ParsedRecordDto());

    // Act
    recordsService.createRecord(marc);

    // Assert
    var value = marcCaptor.getValue();
    assertThat(value.getFields())
      .describedAs("Retain '001' for Authority, but remove for others, remove empty and '999' fields across the board")
      .containsExactlyElementsOf(output);
  }

  public static Stream<Arguments> createRecordCleanupTestData() {
    // Should keep 001 for Authority
    var field001 = new FieldItem().tag("001").content("001-content");
    var fieldNormal = new FieldItem().tag("100").content("$a 100-content");
    var fieldEmpty = new FieldItem().tag("200").content("");
    var field999 = new FieldItem().tag("999").indicators(List.of("f", "f")).content("$a 999-content");
    return Stream.of(
      Arguments.of(
        MarcFormat.AUTHORITY,
        List.of(field001, fieldNormal, fieldEmpty, field999),
        List.of(field001, fieldNormal)
      ),
      Arguments.of(
        MarcFormat.BIBLIOGRAPHIC,
        List.of(field001, fieldNormal, fieldEmpty, field999),
        List.of(fieldNormal)
      ),
      Arguments.of(
        MarcFormat.HOLDINGS,
        List.of(field001, fieldNormal, fieldEmpty, field999),
        List.of(fieldNormal)
      )
    );
  }

  private QuickMarcEdit createQuickMarcEdit() {
    var edit = new QuickMarcEdit();
    edit.setParsedRecordId(PARSED_RECORD_ID);
    edit.setExternalId(EXTERNAL_ID);
    edit.setSourceVersion(CURRENT_VERSION);
    edit.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    edit.setLeader("00000nam  a2200000n  4500");
    edit.addFieldsItem(new FieldItem().tag("245").content("$a Test Title"));
    return edit;
  }

  private SourceRecord createSourceRecord() {
    var newRecord = new SourceRecord();
    newRecord.setRecordId(PARSED_RECORD_ID);
    newRecord.setExternalIdsHolder(new org.folio.qm.client.model.ExternalIdsHolder());
    newRecord.setGeneration(CURRENT_VERSION);
    return newRecord;
  }
}
