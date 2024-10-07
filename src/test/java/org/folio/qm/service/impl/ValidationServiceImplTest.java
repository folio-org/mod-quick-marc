package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.MISSING_FIELD;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.MISSING_SUBFIELD;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.NON_REPEATABLE_FIELD;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.UNDEFINED_SUBFIELD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.folio.qm.converter.MarcQmToValidatableRecordConverter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.exception.MarcRecordValidationException;
import org.folio.qm.service.MarcSpecificationService;
import org.folio.qm.validation.SkippedValidationError;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.validation.SpecificationGuidedValidator;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

  private static final String SUBFIELD_ERROR_MESSAGE = "Subfield 'a' is required.";
  private static final String FIELD_ERROR_MESSAGE = "Field 001 is required.";
  private static final String NON_REPEATABLE_FIELD_ERROR_MESSAGE = "Field is non-repeatable.";
  private static final String VALIDATION_ERROR_PATH = "001[0]";

  private @Mock MarcSpecificationService marcSpecificationService;
  private @Mock SpecificationGuidedValidator validatableRecordValidator;
  private @Mock MarcQmToValidatableRecordConverter converter;

  private @InjectMocks ValidationServiceImpl service;

  @EnumSource(value = MarcRuleCode.class,
              mode = EnumSource.Mode.INCLUDE,
              names = {"INVALID_INDICATOR", "UNDEFINED_INDICATOR"})
  @ParameterizedTest
  void validate_shouldModifyIndicatorErrorMessage(MarcRuleCode marcRuleCode) {
    var error = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
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

  @ParameterizedTest
  @MethodSource("getSkippedValidationErrors")
  void validate_shouldValidateWithoutIssues(List<SkippedValidationError> skippedValidationErrors) {
    var marcRecord = new BaseMarcRecord().marcFormat(MarcFormat.BIBLIOGRAPHIC);
    when(validatableRecordValidator.validate(any(), any())).thenReturn(Collections.emptyList());
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());
    when(converter.convert(marcRecord)).thenReturn(new ValidatableRecord());

    assertDoesNotThrow(() -> service.validateMarcRecord(marcRecord, skippedValidationErrors));
  }

  @ParameterizedTest
  @MethodSource("getSkippedValidationErrors")
  void validate_shouldThrowMarcRecordValidationException(List<SkippedValidationError> skippedValidationErrors) {
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

    var ex = assertThrows(
      MarcRecordValidationException.class, () -> service.validateMarcRecord(marcRecord, skippedValidationErrors));
    var result = ex.getValidationResult().getIssues();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertThat(result.get(0).getMessage()).isEqualTo(SUBFIELD_ERROR_MESSAGE);
  }

  @ParameterizedTest
  @MethodSource("getSkippedValidationErrors")
  void validate_shouldIgnoreWarnValidationIssue(List<SkippedValidationError> skippedValidationErrors) {
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

    assertDoesNotThrow(() -> service.validateMarcRecord(marcRecord, skippedValidationErrors));
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
    var skippedValidationFields = List.of(new SkippedValidationError(TAG_001_CONTROL_FIELD, MISSING_FIELD));

    assertDoesNotThrow(() -> service.validateMarcRecord(marcRecord, skippedValidationFields));
  }

  @Test
  void validate_shouldThrowValidationExceptionIf001NonRepeatableField() {
    var error = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
      .severity(SeverityType.ERROR)
      .definitionType(DefinitionType.FIELD)
      .ruleCode(NON_REPEATABLE_FIELD.getCode())
      .message(NON_REPEATABLE_FIELD_ERROR_MESSAGE)
      .build();
    var marcRecord = new BaseMarcRecord().marcFormat(MarcFormat.BIBLIOGRAPHIC);
    when(validatableRecordValidator.validate(any(), any())).thenReturn(List.of(error));
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());
    when(converter.convert(marcRecord)).thenReturn(new ValidatableRecord());

    var ex = assertThrows(
      MarcRecordValidationException.class, () -> service.validateMarcRecord(marcRecord, null));
    var result = ex.getValidationResult().getIssues();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertThat(result.get(0).getMessage()).isEqualTo(NON_REPEATABLE_FIELD_ERROR_MESSAGE);
  }

  @Test
  void validate_shouldNotThrowValidationExceptionIfErrorsAreSkipped() {
    var nonRepeatableFieldError = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
      .severity(SeverityType.ERROR)
      .definitionType(DefinitionType.FIELD)
      .ruleCode(NON_REPEATABLE_FIELD.getCode())
      .message(NON_REPEATABLE_FIELD_ERROR_MESSAGE)
      .build();

    var missingFieldError = ValidationError.builder()
      .path(VALIDATION_ERROR_PATH)
      .severity(SeverityType.ERROR)
      .definitionType(DefinitionType.FIELD)
      .ruleCode(MISSING_FIELD.getCode())
      .message(FIELD_ERROR_MESSAGE)
      .build();
    var marcRecord = new BaseMarcRecord().marcFormat(MarcFormat.BIBLIOGRAPHIC);
    when(validatableRecordValidator.validate(any(), any())).thenReturn(
      List.of(nonRepeatableFieldError, missingFieldError));
    when(marcSpecificationService.getSpecification(any())).thenReturn(new SpecificationDto());
    when(converter.convert(marcRecord)).thenReturn(new ValidatableRecord());
    var skippedValidationFields = List.of(
      new SkippedValidationError(TAG_001_CONTROL_FIELD, NON_REPEATABLE_FIELD),
      new SkippedValidationError(TAG_001_CONTROL_FIELD, MISSING_FIELD));

    assertDoesNotThrow(() -> service.validateMarcRecord(marcRecord, skippedValidationFields));
  }

  private static Stream<Arguments> getSkippedValidationErrors() {
    return Stream.of(
      arguments(List.of(new SkippedValidationError(TAG_001_CONTROL_FIELD, MISSING_FIELD))),
      arguments(Collections.emptyList()));
  }
}
