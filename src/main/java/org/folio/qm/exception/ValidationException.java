package org.folio.qm.exception;

import org.folio.tenant.domain.dto.Error;
import org.springframework.http.HttpStatus;

/**
 * Custom exception for validation errors.
 */
public class ValidationException extends QuickMarcException {

  public ValidationException(Error error) {
    super(error);
  }

  public ValidationException(Exception ex) {
    super(ex);
  }

  public int getStatus() {
    return HttpStatus.BAD_REQUEST.value();
  }

}
