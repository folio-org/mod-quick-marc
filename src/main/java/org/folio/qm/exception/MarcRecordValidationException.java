package org.folio.qm.exception;

import java.util.stream.Collectors;
import lombok.Getter;
import org.folio.qm.domain.dto.ValidationResult;

@Getter
public class MarcRecordValidationException extends RuntimeException {

  private final transient ValidationResult validationResult;

  public MarcRecordValidationException(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  @Override
  public String getMessage() {
    return "Record validation failed: "
           + validationResult.getIssues().stream()
             .map(Object::toString)
             .collect(Collectors.joining(System.lineSeparator()));
  }
}
