package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.INVALID_INDICATOR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.service.MarcSpecificationService;
import org.folio.qm.validation.ValidationRule;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.validation.SpecificationGuidedValidator;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

  private @Mock List<ValidationRule> validationRules;
  private @Mock MarcSpecificationService marcSpecificationService;
  private @Mock SpecificationGuidedValidator validatableRecordValidator;

  private @InjectMocks ValidationServiceImpl service;

  @Test
  void validate_shouldModifyIndicatorErrorMessage() {
    var error = ValidationError.builder()
      .path("path[0]")
      .severity(SeverityType.ERROR)
      .definitionType(DefinitionType.INDICATOR)
      .ruleCode(INVALID_INDICATOR.getCode())
      .message("Indicator must contain one character and can only accept numbers 0-9, letters a-z or a '#'.")
      .build();
    when(validatableRecordValidator.validate(any(), any())).thenReturn(List.of(error));
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());

    var result = service.validate(new ValidatableRecord());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getMessage())
      .isEqualTo("Indicator must contain one character and can only accept numbers 0-9, letters a-z or a '\\'.");
  }
}
