package org.folio.qm.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.folio.qm.util.JsonUtils.objectToJsonString;

import java.util.UUID;
import org.folio.qm.domain.dto.InitJobExecutionsRqDto;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.ProfileInfo;
import org.folio.qm.domain.dto.RawRecordDto;
import org.folio.qm.domain.dto.RawRecordsDto;
import org.folio.qm.domain.dto.RawRecordsMetadata;
import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;

public interface DataImportJobService {

  UUID executeDataImportJob(ParsedRecordDto recordDto, JobProfileAction action);

  static ProfileInfo toJobProfileInfo(JobProfile jobProfile) {
    return new ProfileInfo()
      .id(jobProfile.getProfileId())
      .name(jobProfile.getProfileName())
      .hidden(true)
      .dataType(ProfileInfo.DataTypeEnum.MARC);
  }

  static InitJobExecutionsRqDto getDefaultJodExecutionDto(UUID userId, JobProfile jobProfile) {
    return new InitJobExecutionsRqDto()
      .jobProfileInfo(toJobProfileInfo(jobProfile))
      .sourceType(InitJobExecutionsRqDto.SourceTypeEnum.ONLINE)
      .files(emptyList())
      .userId(userId);
  }

  static RawRecordsDto toRawRecordsDto(ParsedRecordDto parsedRecordDto) {
    var jsonString = objectToJsonString(requireNonNull(parsedRecordDto).getParsedRecord().getContent());
    return getRawRecordsBody(new RawRecordDto().record(jsonString), false);
  }

  static RawRecordsDto toLastRawRecordsDto() {
    return getRawRecordsBody(null, true);
  }

  private static RawRecordsDto getRawRecordsBody(RawRecordDto initialRecord, boolean isLast) {
    return new RawRecordsDto()
      .id(UUID.randomUUID())
      .initialRecords(initialRecord == null ? emptyList() : singletonList(initialRecord))
      .recordsMetadata(
        new RawRecordsMetadata()
          .last(isLast)
          .counter(1)
          .total(1)
          .contentType(RawRecordsMetadata.ContentTypeEnum.MARC_JSON));
  }
}
