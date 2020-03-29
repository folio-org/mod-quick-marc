package org.folio.util;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.HttpStatus.HTTP_INTERNAL_SERVER_ERROR;

import java.util.Arrays;
import java.util.List;

import org.folio.HttpStatus;
import org.folio.exception.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Parameter;

import io.vertx.core.json.JsonObject;

public class ErrorUtils {

  private ErrorUtils() {}

  public static Parameter buildErrorParameter(String key, String value) {
    return new Parameter().withKey(key).withValue(value);
  }

  public static List<Parameter> buildErrorParameters(Parameter... parameters) {
    return Arrays.asList(parameters);
  }

  public static Error buildError(int status, String message) {
    return new Error().withCode(HttpStatus.get(status).name()).withMessage(message);
  }

  public static Error buildError(int status, ErrorType type, String message) {
    return new Error().withCode(HttpStatus.get(status).name()).withType(type.getTypeCode()).withMessage(message);
  }

  public static Error buildError(int status, ErrorType type, String message, List<Parameter> parameters) {
    return new Error().withCode(HttpStatus.get(status).name()).withType(type.getTypeCode()).withMessage(message).withParameters(parameters);
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
        String type = errorJsonObject.getString("type");
        if (ErrorType.INTERNAL.getTypeCode().equals(type)) {
          error = errorJsonObject.mapTo(Error.class);
        } else {
          error = new Error().withCode("EXTERNAL_OR_UNDEFINED_ERROR").withMessage("External or undefined error").withType(ErrorType.EXTERNAL_OR_UNDEFINED.getTypeCode());
        }
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

  public enum ErrorType {
    INTERNAL("-1"),
    EXTERNAL_OR_UNDEFINED("-2"),
    UNKNOWN("-3");

    private String code;

    ErrorType(String code) {
      this.code = code;
    }

    public String getTypeCode() {
      return code;
    }
  }
}
