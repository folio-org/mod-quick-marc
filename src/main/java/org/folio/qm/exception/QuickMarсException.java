package org.folio.qm.exception;

import static org.folio.qm.util.ErrorUtils.buildError;

import org.folio.tenant.domain.dto.Error;
import org.folio.qm.util.ErrorUtils;

public abstract class QuickMarсException extends RuntimeException {

  private final transient Error error;

  protected QuickMarсException(Error error) {
    this.error = error;
  }

  protected QuickMarсException(Exception ex) {
    if (ex instanceof QuickMarсException) {
      QuickMarсException cex = (QuickMarсException) ex;
      this.error = cex.getError();
    } else {
      this.error = buildError(ErrorUtils.ErrorType.INTERNAL, ex.getClass().getSimpleName() + ": Generic Error");
    }
  }

  public Error getError() {
    return error;
  }

  public abstract int getStatus();

}
