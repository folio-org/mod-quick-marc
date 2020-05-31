package org.folio.exception;

import io.vertx.core.json.JsonObject;
import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.Error;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

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

  public JsonObject getError() {
    return errorJson;
  }

  public int getCode() {
    return code;
  }
}
