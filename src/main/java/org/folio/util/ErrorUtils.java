package org.folio.util;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.HttpStatus.HTTP_INTERNAL_SERVER_ERROR;
import static org.folio.HttpStatus.HTTP_UNPROCESSABLE_ENTITY;

import java.util.Objects;

import org.folio.HttpStatus;
import org.folio.exception.HttpException;
import org.folio.rest.jaxrs.model.Error;

import io.vertx.core.json.JsonObject;

public class ErrorUtils {

  private ErrorUtils() {}

  public static Error buildError(int status, String message) {
    return new Error().withCode(HttpStatus.get(status).name()).withMessage(message);
  }

  public static Error buildError(int status, ErrorType type, String message) {
    return new Error().withCode(HttpStatus.get(status).name()).withType(type.getTypeCode()).withMessage(message);
  }

  public static javax.ws.rs.core.Response getErrorResponse(Throwable throwable) {
    return buildErrorResponse(throwable);
  }

  private static javax.ws.rs.core.Response buildErrorResponse(Throwable throwable) {
    int status;
    Error error;
    final Throwable cause = throwable.getCause();
    try {
      if (cause instanceof HttpException) {
        status = ((HttpException) cause).getCode();
        JsonObject errorJsonObject = ((HttpException) cause).getError();
        error = resolveError(errorJsonObject);
      } else if (cause instanceof IllegalArgumentException) {
        status = HTTP_UNPROCESSABLE_ENTITY.toInt();
        error = buildError(status, cause.getMessage());
      } else {
        status = HTTP_INTERNAL_SERVER_ERROR.toInt();
        error = buildError(status, ErrorType.INTERNAL, "Internal server error");
      }
    } catch (Exception e) {
      status = HTTP_INTERNAL_SERVER_ERROR.toInt();
      error = buildError(status, ErrorType.UNKNOWN, "Internal server error");
    }
    return javax.ws.rs.core.Response.status(status).header(CONTENT_TYPE, APPLICATION_JSON).type(APPLICATION_JSON).entity(error).build();
  }

  private static Error resolveError(JsonObject errorJsonObject) {
    Error error;
    try {
      error = errorJsonObject.mapTo(Error.class);
      String type = errorJsonObject.getString("type");
      if (!Objects.equals(ErrorType.INTERNAL.getTypeCode(), type)) {
        error.withType(ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode());
      }
    } catch(Exception e) {
      error = new Error().withCode("EXTERNAL_OR_UNDEFINED_ERROR").withMessage("External or undefined error").withType(ErrorType.EXTERNAL_OR_UNDEFINED.getTypeCode());
    }
    return error;
  }

  public enum ErrorType {
    INTERNAL("-1"),
    FOLIO_EXTERNAL_OR_UNDEFINED("-2"),
    EXTERNAL_OR_UNDEFINED("-3"),
    UNKNOWN("-4");

    private final String code;

    ErrorType(String code) {
      this.code = code;
    }

    public String getTypeCode() {
      return code;
    }
  }
}
