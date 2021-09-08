package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_EMPTY_FIELDS;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.ap.internal.util.Strings;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.converter.MarcConverterFactory;
import org.folio.qm.converter.impl.MarcBibliographicQmConverter;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.util.ChangeManagerPayloadUtils;
import org.folio.qm.util.JobExecutionProfileProperties;
import org.folio.rest.jaxrs.model.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.InitJobExecutionsRsDto;
import org.folio.rest.jaxrs.model.JobExecution;
import org.folio.rest.jaxrs.model.JobProfileInfo;
import org.folio.spring.FolioExecutionContext;

@ExtendWith({
  MockitoExtension.class,
  RandomBeansExtension.class
})
class MarcRecordsServiceImplTest {

  @InjectMocks
  private MarcRecordsServiceImpl service;
  @Mock
  private ValidationService validationService;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private SRMChangeManagerClient srmClient;
  @Mock
  private CreationStatusService statusService;
  @Mock
  private CreationStatusMapper statusMapper;
  @Mock
  private JobExecutionProfileProperties props;

  @Mock
  private MarcConverterFactory converterFactory;
  @Spy
  private MarcBibliographicQmConverter converter;

  @Test
  void shouldRemoveFieldsWhenEmptyContent(@Random JobProfileInfo profile) {
    doNothing().when(validationService).validateUserId(any(FolioExecutionContext.class));
    when(context.getUserId()).thenReturn(UUID.randomUUID());
    when(props.getId()).thenReturn(VALID_JOB_EXECUTION_ID);
    when(props.getName()).thenReturn("test");

    try (MockedStatic<ChangeManagerPayloadUtils> utils = Mockito.mockStatic(ChangeManagerPayloadUtils.class)) {
      utils.when(() -> ChangeManagerPayloadUtils.getDefaultJodExecutionDto(any(), any(JobExecutionProfileProperties.class)))
        .thenReturn(new InitJobExecutionsRqDto());
      utils.when(() -> ChangeManagerPayloadUtils.getDefaultJobProfile(any(JobExecutionProfileProperties.class)))
        .thenReturn(profile);
    }

    var jobExecutions = Collections.singletonList(new JobExecution().withId(VALID_JOB_EXECUTION_ID));
    when(srmClient.postJobExecution(any())).thenReturn(new InitJobExecutionsRsDto().withJobExecutions(jobExecutions));

    when(statusService.save(any(RecordCreationStatus.class))).thenReturn(new RecordCreationStatus());
    when(statusMapper.fromEntity(any(RecordCreationStatus.class))).thenReturn(new CreationStatus());

    when(srmClient.putJobProfileByJobExecutionId(any(String.class), any())).thenReturn(new JobExecution());
    when(statusService.updateByJobExecutionId(any(UUID.class), any(RecordCreationStatusUpdate.class))).thenReturn(
      Boolean.TRUE);

    var quickMarcJson = readQuickMarc(QM_EMPTY_FIELDS)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .externalId(EXISTED_EXTERNAL_ID);

    final Predicate<FieldItem> field999Predicate = qmFields -> qmFields.getTag().equals("999");
    final Predicate<FieldItem> emptyContentPredicate = qmFields -> {
      final var content = qmFields.getContent();
      return content instanceof String && Strings.isEmpty((String) content);
    };

    when(converterFactory.findConverter(any(MarcFormat.class))).thenReturn(converter);

    doAnswer(InvocationOnMock::callRealMethod).when(converter).convert(argThat(
      quickMarc -> quickMarc.getFields().stream().noneMatch(field999Predicate.or(emptyContentPredicate)))
    );

    doNothing().when(srmClient).postRawRecordsByJobExecutionId(any(),
      argThat(rawRecordsDto -> Objects.nonNull(rawRecordsDto.getId())));

    var actual = service.createNewInstance(quickMarcJson);
    assertThat(actual).isNotNull();
  }
}
