package org.folio.qm.exception;

import lombok.Getter;

import org.folio.qm.validation.ValidationResult;

public class FieldsValidationException extends RuntimeException {

  @Getter
  private final ValidationResult validationResult;

  public FieldsValidationException(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }
}
