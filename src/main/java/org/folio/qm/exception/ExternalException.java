package org.folio.qm.exception;

import org.folio.tenant.domain.dto.Error;
import org.springframework.http.HttpStatus;

public class ExternalException extends QuickMarcException {

  public ExternalException(Error error) {
    super(error);
  }

  public ExternalException(Exception ex) {
    super(ex);
  }

  @Override
  public int getStatus() {
    return HttpStatus.BAD_REQUEST.value();
  }
}
