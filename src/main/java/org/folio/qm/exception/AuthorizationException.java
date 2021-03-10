package org.folio.qm.exception;

import org.springframework.http.HttpStatus;

import org.folio.tenant.domain.dto.Error;

/**
 * Custom exception for Authorization errors
 */

public class AuthorizationException extends QuickMarcException {

  public AuthorizationException(Error error) {
    super(error);
  }

  public AuthorizationException(Exception ex) {
    super(ex);
  }

  @Override
  public int getStatus() {
    return HttpStatus.UNAUTHORIZED.value();
  }
}
