package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.service.DataImportJobService;
import org.folio.qm.service.StatusService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.validation.ValidationResult;
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

  private @Mock DataImportJobService dataImportJobService;
  private @Mock DefaultValuesPopulationService populationService;
  private @Mock ValidationService validationService;
  private @Mock ConversionService conversionService;
  private @Mock StatusService statusService;
  private @Mock CreationStatusMapper statusMapper;
  private @InjectMocks MarcRecordsServiceImpl recordsService;

  private @Captor ArgumentCaptor<QuickMarcCreate> marcCaptor;

  @MethodSource("createNewRecordCleanupTestData")
  @ParameterizedTest
  void createNewRecord_positive_shouldRemoveFieldsBasedOnRecordType(MarcFormat marcFormat, List<FieldItem> input,
                                                                    List<FieldItem> output) {
    // Arrange
    var marc = new QuickMarcCreate().marcFormat(marcFormat);
    marc.getFields().addAll(input);

    doNothing().when(populationService).populate(marc);
    when(validationService.validate(marc)).thenReturn(new ValidationResult(true, Collections.emptyList()));
    when(conversionService.convert(marcCaptor.capture(), eq(ParsedRecordDto.class))).thenReturn(new ParsedRecordDto());
    when(statusService.findByJobExecutionId(any())).thenReturn(Optional.of(new RecordCreationStatus()));
    when(statusMapper.fromEntity(any())).thenReturn(new CreationStatus());
    when(dataImportJobService.executeDataImportJob(any(), any())).thenReturn(UUID.randomUUID());

    // Act
    recordsService.createNewRecord(marc);

    // Assert
    var value = marcCaptor.getValue();
    assertThat(value.getFields())
      .describedAs("Retain '001' for Authority, but remove for others, remove empty and '999' fields across the board")
      .containsExactlyElementsOf(output);
  }

  public static Stream<Arguments> createNewRecordCleanupTestData() {
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
}
