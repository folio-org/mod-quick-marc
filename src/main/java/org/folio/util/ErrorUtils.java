package org.folio.util;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.HttpStatus.HTTP_INTERNAL_SERVER_ERROR;
import static org.folio.HttpStatus.HTTP_UNPROCESSABLE_ENTITY;

import java.util.Objects;

import org.folio.HttpStatus;
import org.folio.exception.ConverterException;
import org.folio.exception.HttpException;
import org.folio.rest.jaxrs.model.Error;

import io.vertx.core.json.JsonObject;

public class ErrorUtils {

  public static final String EXTERNAL_OR_UNDEFINED_ERROR_MSG = "External or undefined error";
  public static final String INTERNAL_SERVER_ERROR_MSG = "Internal server error";

  private ErrorUtils() {
  }

  public static Error buildError(ErrorType type, String message) {
    return new Error().withCode(HTTP_UNPROCESSABLE_ENTITY.name()).withType(type.getTypeCode()).withMessage(message);
  }

  public static Error buildError(int status, String message) {
    return new Error().withCode(HttpStatus.get(status).name()).withMessage(message);
  }

  public static Error buildError(int status, ErrorType type, String message) {
    return new Error().withCode(HttpStatus.get(status).name()).withType(type.getTypeCode()).withMessage(message);
  }

  public static Error buildError(ErrorCodes code, ErrorType type, String message) {
    return new Error().withCode(code.name()).withType(type.getTypeCode()).withMessage(message);
  }

  public static Error buildGenericError() {
    return new Error()
      .withCode(String.valueOf(HTTP_INTERNAL_SERVER_ERROR.toInt()))
      .withType(ErrorType.INTERNAL.getTypeCode())
      .withMessage(INTERNAL_SERVER_ERROR_MSG);
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
      } else if (cause instanceof ConverterException) {
        ConverterException e = (ConverterException) cause;
        error = e.getError().mapTo(Error.class);
        status = e.getCode();
      } else {
        status = HTTP_INTERNAL_SERVER_ERROR.toInt();
        error = buildError(status, ErrorType.INTERNAL, INTERNAL_SERVER_ERROR_MSG);
      }
    } catch (Exception e) {
      status = HTTP_INTERNAL_SERVER_ERROR.toInt();
      error = buildError(status, ErrorType.UNKNOWN, INTERNAL_SERVER_ERROR_MSG);
    }
    return javax.ws.rs.core.Response.status(status)
      .header(CONTENT_TYPE, APPLICATION_JSON)
      .type(APPLICATION_JSON)
      .entity(error)
      .build();
  }

  private static Error resolveError(JsonObject errorJsonObject) {
    Error error;
    try {
      error = errorJsonObject.mapTo(Error.class);
      String type = errorJsonObject.getString("type");
      if (!Objects.equals(ErrorType.INTERNAL.getTypeCode(), type)) {
        error.withType(ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode());
      }
    } catch (Exception e) {
      error = new Error().withCode(ErrorType.EXTERNAL_OR_UNDEFINED.name())
        .withMessage(EXTERNAL_OR_UNDEFINED_ERROR_MSG)
        .withType(ErrorType.EXTERNAL_OR_UNDEFINED.getTypeCode());
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
