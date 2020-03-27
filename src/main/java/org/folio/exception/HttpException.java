package org.folio.exception;

import io.vertx.core.json.JsonObject;

public class HttpException extends RuntimeException {

  private static final long serialVersionUID = 8109197948434861504L;

  private final int code;
  private final transient JsonObject error;

  public HttpException(int code, JsonObject error) {
    this.code = code;
    this.error = error;
  }

  public int getCode() {
    return code;
  }

  public JsonObject getError() {
    return error;
  }
}
