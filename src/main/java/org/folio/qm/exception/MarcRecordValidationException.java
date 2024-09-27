package org.folio.qm.exception;

import lombok.Getter;
import org.folio.qm.domain.dto.ValidationResult;

@Getter
public class MarcRecordValidationException extends RuntimeException {

  private final transient ValidationResult validationResult;

  public MarcRecordValidationException(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }
}
