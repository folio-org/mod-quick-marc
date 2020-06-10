package org.folio.exception;

import io.vertx.core.json.JsonObject;
import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.Error;
import org.folio.util.ErrorUtils;

import static org.folio.util.ErrorUtils.buildError;

/**
 * Custom exception for QuickMarcJson <-> ParsedRecordDto converting errors
 */
public class ConverterException extends RuntimeException {

  private static final long serialVersionUID = 3199267948434461515L;

  private final transient JsonObject errorJson;
  private final int code;

  public ConverterException(Error error) {
    code = HttpStatus.HTTP_UNPROCESSABLE_ENTITY.toInt();
    errorJson = JsonObject.mapFrom(error);
  }

  public ConverterException(Exception ex, Class<?> clazz) {
    if (ex instanceof ConverterException) {
      ConverterException cex = (ConverterException) ex;
      errorJson = cex.getError();
      code = cex.getCode();
    } else {
      errorJson = JsonObject.mapFrom(buildError(ErrorUtils.ErrorType.INTERNAL,clazz.getSimpleName() + ": Generic Error"));
      code = HttpStatus.HTTP_UNPROCESSABLE_ENTITY.toInt();
    }
  }

  public JsonObject getError() {
    return errorJson;
  }

  public int getCode() {
    return code;
  }
}
