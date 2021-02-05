package org.folio.qm.util;


import org.springframework.http.HttpStatus;

import org.folio.tenant.domain.dto.Error;

public final class ErrorUtils {

  private ErrorUtils() {
  }

  public static Error buildError(ErrorType type, String message) {
    return new Error().code(HttpStatus.INTERNAL_SERVER_ERROR.name()).type(type.getTypeCode()).message(message);
  }

  public static Error buildError(int status, String message) {
    return new Error().code(HttpStatus.valueOf(status).name()).message(message);
  }

  public static Error buildError(int status, ErrorType type, String message) {
    return new Error().code(HttpStatus.valueOf(status).name()).type(type.getTypeCode()).message(message);
  }

  public static Error buildError(ErrorCodes code, ErrorType type, String message) {
    return new Error().code(code.name()).type(type.getTypeCode()).message(message);
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
