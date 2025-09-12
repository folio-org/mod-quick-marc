package org.folio.qm.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class OptimisticLockingException extends RuntimeException {

  public static final String MSG_TEMPLATE = "Cannot update record %s because it has been changed (optimistic locking): "
                                            + "Stored _version is %s, _version of request is %s";
  private final UUID recordId;
  private final int storedVersion;
  private final int currentVersion;

  public OptimisticLockingException(UUID recordId, int storedVersion, int currentVersion) {
    this.recordId = recordId;
    this.storedVersion = storedVersion;
    this.currentVersion = currentVersion;
  }

  @Override
  public String getMessage() {
    return MSG_TEMPLATE.formatted(recordId, storedVersion, currentVersion);
  }
}
