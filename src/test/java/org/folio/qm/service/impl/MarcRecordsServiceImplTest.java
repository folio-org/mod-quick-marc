package org.folio.qm.service.impl;

import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.converter.MarcConverterFactory;
import org.folio.qm.converter.impl.MarcBibliographicQmConverter;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.JobProfileService;
import org.folio.qm.support.types.UnitTest;
import org.folio.spring.FolioExecutionContext;

@UnitTest
@ExtendWith({
  MockitoExtension.class,
  RandomBeansExtension.class
})
class MarcRecordsServiceImplTest {

  @Mock
  private FolioExecutionContext context;
  @Mock
  private SRMChangeManagerClient srmClient;
  @Mock
  private CreationStatusService statusService;
  @Mock
  private CreationStatusMapper statusMapper;
  @Mock
  private JobProfileService jobProfileService;

  @Mock
  private MarcConverterFactory converterFactory;
  @Spy
  private MarcBibliographicQmConverter converter;

  @InjectMocks
  private DataImportJobServiceImpl recordActionService;

//  @Test
//  void shouldRemoveFieldsWhenEmptyContent(@Random JobProfileInfo profile) {
//    when(context.getUserId()).thenReturn(UUID.randomUUID());
//    var jobProfile = new JobProfile();
//    jobProfile.setProfileId(VALID_JOB_EXECUTION_ID);
//    jobProfile.setProfileName("test");
//    when(jobProfileService.getJobProfile(MARC_BIBLIOGRAPHIC, CREATE)).thenReturn(jobProfile);
//
//    try (MockedStatic<ChangeManagerPayloadUtils> utils = Mockito.mockStatic(ChangeManagerPayloadUtils.class)) {
//      utils.when(() -> ChangeManagerPayloadUtils.getDefaultJodExecutionDto(any(),
//          any(JobExecutionProfileProperties.ProfileOptions.class)))
//        .thenReturn(new InitJobExecutionsRqDto());
//      utils.when(
//          () -> ChangeManagerPayloadUtils.getDefaultJobProfile(any(JobExecutionProfileProperties.ProfileOptions.class)))
//        .thenReturn(profile);
//    }
//
//    var jobExecutions = Collections.singletonList(new JobExecution().withId(String.valueOf(VALID_JOB_EXECUTION_ID)));
//    when(srmClient.postJobExecution(any())).thenReturn(new InitJobExecutionsRsDto().withJobExecutions(jobExecutions));
//
//    when(statusService.save(any(RecordCreationStatus.class))).thenReturn(new RecordCreationStatus());
//    when(statusMapper.fromEntity(any(RecordCreationStatus.class))).thenReturn(new CreationStatus());
//
//    when(srmClient.putJobProfileByJobExecutionId(any(String.class), any())).thenReturn(new JobExecution());
//    when(statusService.updateByJobExecutionId(any(UUID.class), any(RecordCreationStatusUpdate.class))).thenReturn(
//      Boolean.TRUE);
//
//    var quickMarcJson = readQuickMarc(QM_EMPTY_FIELDS)
//      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
//      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
//      .externalId(EXISTED_EXTERNAL_ID);
//
//    final Predicate<FieldItem> field999Predicate = qmFields -> qmFields.getTag().equals("999");
//    final Predicate<FieldItem> emptyContentPredicate = qmFields -> {
//      final var content = qmFields.getContent();
//      return content instanceof String && Strings.isEmpty((String) content);
//    };
//
//    when(converterFactory.findConverter(any(MarcFormat.class))).thenReturn(converter);
//
//    doAnswer(InvocationOnMock::callRealMethod).when(converter).convert(argThat(
//      quickMarc -> quickMarc.getFields().stream().noneMatch(field999Predicate.or(emptyContentPredicate)))
//    );
//
//    doNothing().when(srmClient).postRawRecordsByJobExecutionId(any(),
//      argThat(rawRecordsDto -> Objects.nonNull(rawRecordsDto.getId())));
//
//    var actual = recordActionService.executeDataImportJob(quickMarcJson, CREATE);
//    assertThat(actual).isNotNull();
//  }
}
