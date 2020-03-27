package org.folio.util;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Arrays;
import java.util.List;

import org.folio.HttpStatus;
import org.folio.exception.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.jaxrs.model.Parameter;

import io.vertx.core.json.JsonObject;

public class ErrorUtils {

  public static Parameter buildErrorParameter(String key, String value) {
    return new Parameter().withKey(key)
      .withValue(value);
  }

  public static List<Parameter> buildErrorParameters(Parameter... parameters) {
    return Arrays.asList(parameters);
  }

  public static Error buildError(int code, String message, List<Parameter> parameters) {
    Error error = new Error();
    error.setCode(HttpStatus.get(code)
      .name());
    error.setMessage(message);
    error.setParameters(parameters);
    return error;
  }

  public static Errors buildErrors(Error... errors) {
    return new Errors().withErrors(Arrays.asList(errors));
  }

  public static javax.ws.rs.core.Response getErrorResponse(Throwable throwable) {
    return buildErrorResponse(throwable);
  }

  private static javax.ws.rs.core.Response buildErrorResponse(Throwable throwable) {
    javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.serverError();

    final Throwable cause = throwable.getCause();
    final JsonObject jsonObjectError;
    Error error = new Error();
    int code = 500;

    if (cause instanceof HttpException) {
      code = ((HttpException) cause).getCode();
      jsonObjectError = ((HttpException) cause).getError();
      jsonObjectError.forEach(entry -> error.getParameters()
        .add(new Parameter().withKey(entry.getKey())
          .withValue(entry.getValue()
            .toString())));
      String message = jsonObjectError.getString("errorMessage");
      error.setCode(HttpStatus.get(code)
        .name());
      error.setMessage(message);

      responseBuilder = javax.ws.rs.core.Response.status(code)
        .entity(jsonObjectError);
    }

    return responseBuilder.header(CONTENT_TYPE, APPLICATION_JSON)
      .status(code)
      .entity(error)
      .build();
  }

}
