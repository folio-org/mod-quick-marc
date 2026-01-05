package org.folio.qm.service.validation;

import java.util.List;

public record ValidationResult(boolean isValid, List<ValidationError> errors) {

}
