package org.folio.qm.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.folio.qm.service.validation.ValidationError;
import org.folio.qm.service.validation.ValidationResult;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class FieldsValidationExceptionTest {

  @Test
  void shouldCreateExceptionWithValidationResult() {
    var validationError = new ValidationError("tag", "Test error");
    var validationResult = new ValidationResult(false, List.of(validationError));

    var exception = new FieldsValidationException(validationResult);

    assertNotNull(exception);
    assertSame(validationResult, exception.getValidationResult());
  }

  @Test
  void shouldPreserveValidationErrors() {
    var errors = List.of(
        new ValidationError("001", "Error 1"),
        new ValidationError("245", "Error 2")
    );
    var validationResult = new ValidationResult(false, errors);

    var exception = new FieldsValidationException(validationResult);

    assertNotNull(exception.getValidationResult());
    assertEquals(2, exception.getValidationResult().errors().size());
    assertEquals("Error 1", exception.getValidationResult().errors().get(0).message());
    assertEquals("Error 2", exception.getValidationResult().errors().get(1).message());
  }

  @Test
  void shouldHandleValidValidationResult() {
    var validationResult = new ValidationResult(true, List.of());

    var exception = new FieldsValidationException(validationResult);

    assertNotNull(exception.getValidationResult());
    assertTrue(exception.getValidationResult().isValid());
    assertEquals(0, exception.getValidationResult().errors().size());
  }
}
