package org.folio.qm.util;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.UUID;

import lombok.experimental.UtilityClass;

import org.folio.qm.domain.entity.JobProfile;
import org.folio.rest.jaxrs.model.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.JobProfileInfo;
import org.folio.rest.jaxrs.model.JobProfileInfo.DataType;

@UtilityClass
public class ChangeManagerPayloadUtils {

  public static JobProfileInfo getDefaultJobProfile(JobProfile jobProfile) {
    return new JobProfileInfo()
      .withId(String.valueOf(jobProfile.getProfileId()))
      .withName(jobProfile.getProfileName())
      .withHidden(true)
      .withDataType(DataType.MARC);
  }

  public static InitJobExecutionsRqDto getDefaultJodExecutionDto(UUID userId, JobProfile jobProfile) {
    return new InitJobExecutionsRqDto()
      .withJobProfileInfo(getDefaultJobProfile(jobProfile))
      .withSourceType(InitJobExecutionsRqDto.SourceType.ONLINE)
      .withFiles(emptyList())
      .withUserId(userId.toString());
  }

}
