package org.folio.qm.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.UUID;

import lombok.experimental.UtilityClass;

import org.folio.rest.jaxrs.model.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.InitialRecord;
import org.folio.rest.jaxrs.model.JobProfileInfo;
import org.folio.rest.jaxrs.model.RawRecordsDto;
import org.folio.rest.jaxrs.model.RecordsMetadata;

@UtilityClass
public class ChangeManagerPayloadUtils {

  public static JobProfileInfo getDefaultJobProfile(JobExecutionProfileProperties profileProperties) {
    return new JobProfileInfo()
      .withId(profileProperties.getId())
      .withName(profileProperties.getName())
      .withDataType(JobProfileInfo.DataType.MARC);
  }

  public static InitJobExecutionsRqDto getDefaultJodExecutionDto(String userId, JobExecutionProfileProperties profileProperties) {
    return new InitJobExecutionsRqDto()
      .withJobProfileInfo(getDefaultJobProfile(profileProperties))
      .withSourceType(InitJobExecutionsRqDto.SourceType.ONLINE)
      .withFiles(emptyList())
      .withUserId(userId);
  }

  public static RawRecordsDto getRawRecordsBody(InitialRecord initialRecord, boolean isLast) {
    return new RawRecordsDto()
      .withId(UUID.randomUUID().toString())
      .withInitialRecords(initialRecord == null ? emptyList(): singletonList(initialRecord))
      .withRecordsMetadata(
        new RecordsMetadata()
          .withLast(isLast)
          .withCounter(1)
          .withTotal(1)
          .withContentType(RecordsMetadata.ContentType.MARC_JSON));
  }
}
