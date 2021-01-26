package org.folio.qm.exception;

import static org.folio.qm.util.ErrorUtils.buildError;

import org.folio.qm.domain.dto.Error;
import org.folio.qm.util.ErrorUtils;

public abstract class QuickMarkException extends RuntimeException {

  private final transient Error error;

  protected QuickMarkException(Error error) {
    this.error = error;
  }

  protected QuickMarkException(Exception ex) {
    if (ex instanceof QuickMarkException) {
      QuickMarkException cex = (QuickMarkException) ex;
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
