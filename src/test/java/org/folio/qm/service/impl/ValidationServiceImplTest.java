package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.service.MarcSpecificationService;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.validation.SpecificationGuidedValidator;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

  private @Mock MarcSpecificationService marcSpecificationService;
  private @Mock SpecificationGuidedValidator validatableRecordValidator;

  private @InjectMocks ValidationServiceImpl service;

  @EnumSource(value = MarcRuleCode.class,
              mode = EnumSource.Mode.INCLUDE,
              names = {"INVALID_INDICATOR", "UNDEFINED_INDICATOR"})
  @ParameterizedTest
  void validate_shouldModifyIndicatorErrorMessage(MarcRuleCode marcRuleCode) {
    var error = ValidationError.builder()
      .path("path[0]")
      .severity(SeverityType.ERROR)
      .definitionType(DefinitionType.INDICATOR)
      .ruleCode(marcRuleCode.getCode())
      .message("Message that contains '#'.")
      .build();
    when(validatableRecordValidator.validate(any(), any())).thenReturn(List.of(error));
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());

    var result = service.validate(new ValidatableRecord());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getMessage())
      .isEqualTo("Message that contains '\\'.");
  }
}
