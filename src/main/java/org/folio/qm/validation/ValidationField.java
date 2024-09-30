package org.folio.qm.validation;

import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

public record ValidationField(String tagName, boolean isSkipped, MarcRuleCode ruleCode) {
}
