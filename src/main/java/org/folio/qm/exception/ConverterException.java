package org.folio.qm.exception;

import static org.folio.qm.util.ErrorUtils.buildError;

import org.springframework.http.HttpStatus;

import org.folio.qm.domain.dto.Error;
import org.folio.qm.util.ErrorUtils;

/**
 * Custom exception for QuickMarc <-> ParsedRecordDto converting errors
 */
public class ConverterException extends RuntimeException {

  private static final long serialVersionUID = 3199267948434461515L;

  private final transient Error error;
  private final int status;

  public ConverterException(Error error) {
    this.status = HttpStatus.UNPROCESSABLE_ENTITY.value();
    this.error = error;
  }

  public ConverterException(Exception ex, Class<?> clazz) {
    if (ex instanceof ConverterException) {
      ConverterException cex = (ConverterException) ex;
      this.error = cex.getError();
      status = cex.getStatus();
    } else {
      error = buildError(ErrorUtils.ErrorType.INTERNAL, clazz.getSimpleName() + ": Generic Error");
      status = HttpStatus.UNPROCESSABLE_ENTITY.value();
    }
  }

  public Error getError() {
    return error;
  }

  public int getStatus() {
    return status;
  }
}
