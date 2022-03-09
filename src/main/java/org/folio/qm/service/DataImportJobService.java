package org.folio.qm.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import static org.folio.qm.util.JsonUtils.objectToJsonString;

import java.util.UUID;

import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.rest.jaxrs.model.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.InitialRecord;
import org.folio.rest.jaxrs.model.JobProfileInfo;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.folio.rest.jaxrs.model.RawRecordsDto;
import org.folio.rest.jaxrs.model.RecordsMetadata;

public interface DataImportJobService {

  UUID executeDataImportJob(ParsedRecordDto recordDto, JobProfileAction action);

  static JobProfileInfo toJobProfileInfo(JobProfile jobProfile) {
    return new JobProfileInfo()
      .withId(String.valueOf(jobProfile.getProfileId()))
      .withName(jobProfile.getProfileName())
      .withHidden(true)
      .withDataType(JobProfileInfo.DataType.MARC);
  }

  static InitJobExecutionsRqDto getDefaultJodExecutionDto(UUID userId, JobProfile jobProfile) {
    return new InitJobExecutionsRqDto()
      .withJobProfileInfo(toJobProfileInfo(jobProfile))
      .withSourceType(InitJobExecutionsRqDto.SourceType.ONLINE)
      .withFiles(emptyList())
      .withUserId(userId.toString());
  }

  static RawRecordsDto toRawRecordsDto(ParsedRecordDto parsedRecordDto) {
    var jsonString = objectToJsonString(requireNonNull(parsedRecordDto).getParsedRecord().getContent());
    return getRawRecordsBody(new InitialRecord().withRecord(jsonString), false);
  }

  static RawRecordsDto toLastRawRecordsDto() {
    return getRawRecordsBody(null, true);
  }

  private static RawRecordsDto getRawRecordsBody(InitialRecord initialRecord, boolean isLast) {
    return new RawRecordsDto()
      .withId(UUID.randomUUID().toString())
      .withInitialRecords(initialRecord == null ? emptyList() : singletonList(initialRecord))
      .withRecordsMetadata(
        new RecordsMetadata()
          .withLast(isLast)
          .withCounter(1)
          .withTotal(1)
          .withContentType(RecordsMetadata.ContentType.MARC_JSON));
  }
}
