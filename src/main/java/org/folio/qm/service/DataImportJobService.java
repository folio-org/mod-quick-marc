package org.folio.qm.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.folio.qm.util.JsonUtils.objectToJsonString;

import java.util.UUID;
import org.folio.qm.client.model.InitJobExecutionsRqDto;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.ProfileInfo;
import org.folio.qm.client.model.RawRecordDto;
import org.folio.qm.client.model.RawRecordsDto;
import org.folio.qm.client.model.RawRecordsMetadata;
import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;

public interface DataImportJobService {

  static ProfileInfo toJobProfileInfo(JobProfile jobProfile) {
    return new ProfileInfo()
      .setId(jobProfile.getProfileId())
      .setName(jobProfile.getProfileName())
      .setHidden(true)
      .setDataType(ProfileInfo.DataTypeEnum.MARC);
  }

  static InitJobExecutionsRqDto getDefaultJodExecutionDto(UUID userId, JobProfile jobProfile) {
    return new InitJobExecutionsRqDto()
      .setJobProfileInfo(toJobProfileInfo(jobProfile))
      .setSourceType(InitJobExecutionsRqDto.SourceTypeEnum.ONLINE)
      .setFiles(emptyList())
      .setUserId(userId);
  }

  static RawRecordsDto toRawRecordsDto(ParsedRecordDto parsedRecordDto) {
    var jsonString = objectToJsonString(requireNonNull(parsedRecordDto).getParsedRecord().getContent());
    return getRawRecordsBody(new RawRecordDto().setRecordData(jsonString), false);
  }

  static RawRecordsDto toLastRawRecordsDto() {
    return getRawRecordsBody(null, true);
  }

  UUID executeDataImportJob(ParsedRecordDto recordDto, JobProfileAction action);

  private static RawRecordsDto getRawRecordsBody(RawRecordDto initialRecord, boolean isLast) {
    return new RawRecordsDto()
      .setId(UUID.randomUUID())
      .setInitialRecords(initialRecord == null ? emptyList() : singletonList(initialRecord))
      .setRecordsMetadata(
        new RawRecordsMetadata()
          .setLast(isLast)
          .setCounter(1)
          .setTotal(1)
          .setContentType(RawRecordsMetadata.ContentTypeEnum.JSON));
  }
}
