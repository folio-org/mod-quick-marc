package org.folio.qm.exception;

import static org.folio.qm.util.ErrorUtils.buildError;

import lombok.Getter;
import org.folio.qm.util.ErrorUtils;
import org.folio.tenant.domain.dto.Error;

@Getter
public abstract class QuickMarcException extends RuntimeException {

  private final transient Error error;

  protected QuickMarcException(Error error) {
    this.error = error;
  }

  protected QuickMarcException(Exception ex) {
    if (ex instanceof QuickMarcException cex) {
      this.error = cex.getError();
    } else {
      this.error = buildError(ErrorUtils.ErrorType.INTERNAL, ex.getClass().getName() + ": " + ex.getMessage());
    }
  }

  public abstract int getStatus();
}
