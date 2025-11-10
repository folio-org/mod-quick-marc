package org.folio.qm.util;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;

@UtilityClass
public class StatusUtils {

  public static RecordCreationStatus getStatus(UUID jobExecutionId, RecordCreationStatusEnum statusName) {
    final RecordCreationStatus status = new RecordCreationStatus();
    status.setJobExecutionId(jobExecutionId);
    status.setStatus(statusName);
    status.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    status.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    return status;
  }

  public static RecordCreationStatusUpdate getStatusInProgress() {
    var status = RecordCreationStatusUpdate.builder();
    status.status(RecordCreationStatusEnum.IN_PROGRESS);
    return status.build();
  }
}
