package org.folio.qm.util;

import java.util.List;
import org.folio.qm.service.validation.ValidationError;
import org.folio.tenant.domain.dto.Error;
import org.folio.tenant.domain.dto.Errors;
import org.folio.tenant.domain.dto.Parameter;
import org.springframework.http.HttpStatus;

public final class ErrorUtils {

  private ErrorUtils() {
  }

  public static Error buildError(ErrorType type, String message) {
    return new Error().code(HttpStatus.INTERNAL_SERVER_ERROR.name()).type(type.getTypeCode()).message(message);
  }

  public static Error buildError(int status, String message) {
    return new Error().code(HttpStatus.valueOf(status).name()).message(message);
  }

  public static Error buildError(ValidationError validationError) {
    var parameter = new Parameter().key(validationError.tag());
    return new Error()
      .type(ErrorType.INTERNAL.getTypeCode())
      .message(validationError.message())
      .parameters(List.of(parameter));
  }

  public static Error buildError(HttpStatus status, ErrorType type, String message) {
    return new Error().code(status.name()).type(type.getTypeCode()).message(message);
  }

  public static Error buildErrors(int status, ErrorType type, String message) {
    return new Error().code(HttpStatus.valueOf(status).name()).type(type.getTypeCode()).message(message);
  }

  public static Error buildErrors(ErrorCodes code, ErrorType type, String message) {
    return new Error().code(code.name()).type(type.getTypeCode()).message(message);
  }

  public static Errors buildErrors(List<ValidationError> validationErrors) {
    var errors = validationErrors.stream()
      .map(ErrorUtils::buildError)
      .toList();
    return new Errors().errors(errors).totalRecords(errors.size());
  }

  public static Error buildInternalError(ErrorCodes code, String message) {
    return buildErrors(code, ErrorType.INTERNAL, message);
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
