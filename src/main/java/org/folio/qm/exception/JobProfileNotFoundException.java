package org.folio.qm.exception;

import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;

public class JobProfileNotFoundException extends RuntimeException {

  private static final String MESSAGE_TEMPLATE = "Job profile for [%s] action and [%s] record type was not found";

  public JobProfileNotFoundException(RecordType recordType, JobProfileAction profileAction) {
    super(String.format(MESSAGE_TEMPLATE, profileAction, recordType));
  }
}
