package org.folio.qm.validation;

import java.util.List;

public record ValidationResult(boolean isValid, List<ValidationError> errors) {

}
