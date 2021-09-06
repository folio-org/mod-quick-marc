package org.folio.qm.validation;

import java.util.List;

import lombok.Value;

@Value
public class ValidationResult {

  boolean isValid;
  List<ValidationError> errors;
}
