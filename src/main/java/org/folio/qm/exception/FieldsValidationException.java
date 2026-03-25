package org.folio.qm.exception;

import lombok.Getter;
import org.folio.qm.service.validation.ValidationResult;

@Getter
public class FieldsValidationException extends RuntimeException {

  private final transient ValidationResult validationResult;

  public FieldsValidationException(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }
}
