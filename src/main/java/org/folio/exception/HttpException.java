package org.folio.exception;

import io.vertx.core.json.JsonObject;
import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.Error;


import static org.apache.commons.lang3.math.NumberUtils.toInt;

/**
 * Custom exception for handling general errors
 */
public class HttpException extends RuntimeException {

  private static final long serialVersionUID = 8109197948434861504L;

  private final int code;
  private final transient JsonObject errorJson;

  public HttpException(int code, JsonObject errorJson) {
    this.code = code;
    this.errorJson = errorJson;
  }

  public HttpException(Error error) {
    this.code = toInt(error.getCode(), HttpStatus.HTTP_INTERNAL_SERVER_ERROR.toInt());
    errorJson = JsonObject.mapFrom(error);
  }

  public int getCode() {
    return code;
  }

  public JsonObject getError() {
    return errorJson;
  }
}
