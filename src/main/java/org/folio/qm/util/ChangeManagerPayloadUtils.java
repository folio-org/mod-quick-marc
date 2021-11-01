package org.folio.qm.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.UUID;

import lombok.experimental.UtilityClass;

import org.folio.qm.config.properties.JobExecutionProfileProperties;
import org.folio.rest.jaxrs.model.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.InitialRecord;
import org.folio.rest.jaxrs.model.JobProfileInfo;
import org.folio.rest.jaxrs.model.JobProfileInfo.DataType;
import org.folio.rest.jaxrs.model.RawRecordsDto;
import org.folio.rest.jaxrs.model.RecordsMetadata;

@UtilityClass
public class ChangeManagerPayloadUtils {

  public static JobProfileInfo getDefaultJobProfile(JobExecutionProfileProperties.ProfileOptions options) {
    return new JobProfileInfo()
      .withId(String.valueOf(options.getId()))
      .withName(options.getName())
      .withDataType(DataType.MARC);
  }

  public static InitJobExecutionsRqDto getDefaultJodExecutionDto(String userId,
                                                                 JobExecutionProfileProperties.ProfileOptions options) {
    return new InitJobExecutionsRqDto()
      .withJobProfileInfo(getDefaultJobProfile(options))
      .withSourceType(InitJobExecutionsRqDto.SourceType.ONLINE)
      .withFiles(emptyList())
      .withUserId(userId);
  }

  public static RawRecordsDto getRawRecordsBody(InitialRecord initialRecord, boolean isLast) {
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
