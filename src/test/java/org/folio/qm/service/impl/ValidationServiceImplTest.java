package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.INVALID_INDICATOR;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.MISSING_FIELD;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.MISSING_SUBFIELD;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.UNDEFINED_SUBFIELD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.folio.qm.converter.MarcQmToValidatableRecordConverter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.exception.MarcRecordValidationException;
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

  private static final String SUBFIELD_ERROR_MESSAGE = "Subfield 'a' is required.";
  private static final String FIELD_ERROR_MESSAGE = "Field 001 is required.";
  private static final String VALIDATION_ERROR_PATH = "path[0]";

  private @Mock List<ValidationRule> validationRules;
  private @Mock MarcSpecificationService marcSpecificationService;
  private @Mock SpecificationGuidedValidator validatableRecordValidator;
  private @Mock MarcQmToValidatableRecordConverter converter;

  private @InjectMocks ValidationServiceImpl service;

  @Test
  void validate_shouldModifyIndicatorErrorMessage() {
    var error = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
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

  @Test
  void validate_shouldValidateWithoutIssues() {
    var marcRecord = new BaseMarcRecord().marcFormat(MarcFormat.BIBLIOGRAPHIC);
    when(validatableRecordValidator.validate(any(), any())).thenReturn(Collections.emptyList());
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());
    when(converter.convert(marcRecord)).thenReturn(new ValidatableRecord());

    assertDoesNotThrow(() -> service.validateMarcRecord(marcRecord, true));
  }

  @Test
  void validate_shouldThrowMarcRecordValidationException() {
    var error = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
      .severity(SeverityType.ERROR)
      .definitionType(DefinitionType.SUBFIELD)
      .ruleCode(MISSING_SUBFIELD.getCode())
      .message(SUBFIELD_ERROR_MESSAGE)
      .build();
    var marcRecord = new BaseMarcRecord().marcFormat(MarcFormat.BIBLIOGRAPHIC);
    when(validatableRecordValidator.validate(any(), any())).thenReturn(List.of(error));
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());
    when(converter.convert(marcRecord)).thenReturn(new ValidatableRecord());

    var ex = assertThrows(MarcRecordValidationException.class, () -> service.validateMarcRecord(marcRecord, true));
    var result = ex.getValidationResult().getIssues();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertThat(result.get(0).getMessage()).isEqualTo(SUBFIELD_ERROR_MESSAGE);
  }

  @Test
  void validate_shouldIgnoreWarnValidationIssue() {
    var error = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
      .severity(SeverityType.WARN)
      .definitionType(DefinitionType.SUBFIELD)
      .ruleCode(UNDEFINED_SUBFIELD.getCode())
      .build();
    var marcRecord = new BaseMarcRecord().marcFormat(MarcFormat.BIBLIOGRAPHIC);
    when(validatableRecordValidator.validate(any(), any())).thenReturn(List.of(error));
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());
    when(converter.convert(marcRecord)).thenReturn(new ValidatableRecord());

    assertDoesNotThrow(() -> service.validateMarcRecord(marcRecord, true));
  }

  @Test
  void validate_shouldIgnoreErrorValidationIssueFor001Field() {
    var error = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
      .severity(SeverityType.ERROR)
      .definitionType(DefinitionType.FIELD)
      .ruleCode(MISSING_FIELD.getCode())
      .message(FIELD_ERROR_MESSAGE)
      .build();
    var marcRecord = new BaseMarcRecord().marcFormat(MarcFormat.BIBLIOGRAPHIC);
    when(validatableRecordValidator.validate(any(), any())).thenReturn(List.of(error));
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());
    when(converter.convert(marcRecord)).thenReturn(new ValidatableRecord());

    assertDoesNotThrow(() -> service.validateMarcRecord(marcRecord, false));
  }
}
