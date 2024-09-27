package org.folio.qm.validation;

import org.folio.qm.domain.dto.ValidationIssue;

public record ValidationIssueWithRuleCode(ValidationIssue validationIssue, String ruleCode) {
}
