package org.folio.qm.exception;


import org.springframework.http.HttpStatus;

import org.folio.tenant.domain.dto.Error;

/**
 * Custom exception for QuickMarc <-> ParsedRecordDto converting errors
 */
public class ConverterException extends QuickMarсException {

  public ConverterException(Error error) {
    super(error);
  }

  public ConverterException(Exception ex) {
    super(ex);
  }

  @Override
  public int getStatus() {
    return HttpStatus.UNPROCESSABLE_ENTITY.value();
  }
}
