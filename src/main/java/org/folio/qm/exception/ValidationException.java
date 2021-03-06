package org.folio.qm.exception;

import org.springframework.http.HttpStatus;

import org.folio.tenant.domain.dto.Error;

/**
 * Custom exception for validation errors
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
