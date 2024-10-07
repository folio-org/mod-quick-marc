package org.folio.qm.validation;

import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

public record SkippedValidationError(String tagName, MarcRuleCode ruleCode) {
}
