package org.folio.qm.util;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.experimental.UtilityClass;

import org.folio.qm.domain.entity.ActionStatus;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.domain.entity.ActionStatusUpdate;
import org.folio.qm.domain.entity.JobProfile;

@UtilityClass
public class StatusUtils {

  public static ActionStatus getStatusInProgress(UUID jobExecutionId, UUID jobProfileId) {
    final ActionStatus status = new ActionStatus();
    status.setJobExecutionId(jobExecutionId);
    status.setStatus(ActionStatusEnum.IN_PROGRESS);
    var jobProfile = new JobProfile();
    jobProfile.setId(jobProfileId);
    status.setJobProfile(jobProfile);
    status.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    status.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    return status;
  }

  public static ActionStatusUpdate getStatusErrorUpdate(String errorMessage) {
    return ActionStatusUpdate.builder()
      .status(ActionStatusEnum.ERROR)
      .errorMessage(errorMessage)
      .build();
  }
}
