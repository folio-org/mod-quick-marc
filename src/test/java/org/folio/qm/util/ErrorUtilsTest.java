package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.folio.qm.service.validation.ValidationError;
import org.folio.qm.util.ErrorUtils.ErrorType;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@UnitTest
class ErrorUtilsTest {

  @Test
  void shouldBuildErrorWithTypeAndMessage() {
    var error = ErrorUtils.buildError(ErrorType.INTERNAL, "Test message");

    assertNotNull(error);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), error.getCode());
    assertEquals(ErrorType.INTERNAL.getTypeCode(), error.getType());
    assertEquals("Test message", error.getMessage());
  }

  @Test
  void shouldBuildErrorWithStatusAndMessage() {
    var error = ErrorUtils.buildError(404, "Not found");

    assertNotNull(error);
    assertEquals(HttpStatus.NOT_FOUND.name(), error.getCode());
    assertEquals("Not found", error.getMessage());
  }

  @Test
  void shouldBuildErrorFromValidationError() {
    var validationError = new ValidationError("tag", "Test validation error");
    
    var error = ErrorUtils.buildError(validationError);

    assertNotNull(error);
    assertEquals(ErrorType.INTERNAL.getTypeCode(), error.getType());
    assertEquals("Test validation error", error.getMessage());
    assertNotNull(error.getParameters());
    assertEquals(1, error.getParameters().size());
    assertEquals("tag", error.getParameters().getFirst().getKey());
  }

  @Test
  void shouldBuildErrorWithHttpStatusTypeAndMessage() {
    var error = ErrorUtils.buildError(HttpStatus.BAD_REQUEST, ErrorType.EXTERNAL_OR_UNDEFINED, "Bad request");

    assertNotNull(error);
    assertEquals(HttpStatus.BAD_REQUEST.name(), error.getCode());
    assertEquals(ErrorType.EXTERNAL_OR_UNDEFINED.getTypeCode(), error.getType());
    assertEquals("Bad request", error.getMessage());
  }

  @Test
  void shouldBuildErrorsWithStatusCodeTypeAndMessage() {
    var error = ErrorUtils.buildErrors(400, ErrorType.INTERNAL, "Validation failed");

    assertNotNull(error);
    assertEquals(HttpStatus.BAD_REQUEST.name(), error.getCode());
    assertEquals(ErrorType.INTERNAL.getTypeCode(), error.getType());
    assertEquals("Validation failed", error.getMessage());
  }

  @Test
  void shouldBuildErrorsWithErrorCodeTypeAndMessage() {
    var error = ErrorUtils.buildErrors(ErrorCodes.ILLEGAL_MARC_FORMAT, ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED, "Error");

    assertNotNull(error);
    assertEquals(ErrorCodes.ILLEGAL_MARC_FORMAT.name(), error.getCode());
    assertEquals(ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode(), error.getType());
    assertEquals("Error", error.getMessage());
  }

  @Test
  void shouldBuildInternalError() {
    var error = ErrorUtils.buildInternalError(ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FIELD, "Internal error");

    assertNotNull(error);
    assertEquals(ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FIELD.name(), error.getCode());
    assertEquals(ErrorType.INTERNAL.getTypeCode(), error.getType());
    assertEquals("Internal error", error.getMessage());
  }

  @Test
  void shouldBuildErrorsFromValidationErrors() {
    var validationErrors = List.of(
        new ValidationError("tag1", "Error 1"),
        new ValidationError("tag2", "Error 2"),
        new ValidationError("tag3", "Error 3")
    );

    var errors = ErrorUtils.buildErrors(validationErrors);

    assertNotNull(errors);
    assertEquals(3, errors.getTotalRecords());
    assertEquals(3, errors.getErrors().size());
    assertEquals("Error 1", errors.getErrors().get(0).getMessage());
    assertEquals("Error 2", errors.getErrors().get(1).getMessage());
    assertEquals("Error 3", errors.getErrors().get(2).getMessage());
  }

  @Test
  void shouldReturnCorrectErrorTypeCodes() {
    assertEquals("-1", ErrorType.INTERNAL.getTypeCode());
    assertEquals("-2", ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode());
    assertEquals("-3", ErrorType.EXTERNAL_OR_UNDEFINED.getTypeCode());
    assertEquals("-4", ErrorType.UNKNOWN.getTypeCode());
  }
}
