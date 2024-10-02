package org.folio.qm.validation;

import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

public record SkippedValidationField(String tagName, MarcRuleCode ruleCode) {
}
