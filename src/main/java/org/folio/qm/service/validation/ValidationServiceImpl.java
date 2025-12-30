package org.folio.qm.service.validation;

import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.INVALID_INDICATOR;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.UNDEFINED_INDICATOR;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.convertion.converter.BaseQuickMarcRecordToValidatableRecordConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidationIssue;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.domain.model.ValidatableRecordDelegate;
import org.folio.qm.exception.MarcRecordValidationException;
import org.folio.qm.service.population.DefaultValuesPopulationService;
import org.folio.qm.service.storage.specification.MarcSpecificationService;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.utils.SpecificationUtils;
import org.folio.rspec.validation.SpecificationGuidedValidator;
import org.marc4j.marc.Record;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Log4j2
public class ValidationServiceImpl implements ValidationService {

  public static final String TAG_NAME_REGEX = "^%s\\[\\d+]";

  private final List<ValidationRule> validationRules;
  private final MarcSpecificationService marcSpecificationService;
  private final SpecificationGuidedValidator validatableRecordValidator;
  private final BaseQuickMarcRecordToValidatableRecordConverter converter;
  private final DefaultValuesPopulationService defaultValuesPopulationService;

  @Override
  public ValidationResult validate(BaseQuickMarcRecord quickMarc) {
    log.debug("validate:: Validating MARC record with format: {}", quickMarc.getMarcFormat());
    var validationErrors = validationRules.stream()
      .filter(rule -> rule.supportFormat(quickMarc.getMarcFormat()))
      .map(rule -> rule.validate(quickMarc))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .toList();

    if (validationErrors.isEmpty()) {
      log.debug("validate:: Validation successful - no errors found");
      return new ValidationResult(true, Collections.emptyList());
    } else {
      log.warn("validate:: Validation failed with {} errors", validationErrors.size());
      return new ValidationResult(false, validationErrors);
    }
  }

  @Override
  public List<ValidationIssue> validate(ValidatableRecord validatableRecord) {
    log.debug("validate:: Validating validatable record with format: {}", validatableRecord.getMarcFormat());
    defaultValuesPopulationService.populate(validatableRecord);
    var specification = marcSpecificationService.getSpecification(validatableRecord.getMarcFormat());
    var issues = validatableRecordValidator.validate(new ValidatableRecordDelegate(validatableRecord), specification)
      .stream()
      .map(validationError -> toValidationIssue(validationError, specification))
      .toList();
    log.debug("validate:: Found {} validation issues", issues.size());
    return issues;
  }

  @Override
  public void validateMarcRecord(BaseQuickMarcRecord marcRecord, List<SkippedValidationError> skippedValidationErrors) {
    if (marcRecord.getMarcFormat() != MarcFormat.HOLDINGS) {
      log.debug("validateMarcRecord:: validate a quickMarc record with format: {}", marcRecord.getMarcFormat());
      var validatableRecord = converter.convert(marcRecord);
      if (validatableRecord == null) {
        log.warn("validateMarcRecord:: Converted validatableRecord is null, skipping validation");
        return;
      }

      var validationIssues = getValidationIssues(validatableRecord, skippedValidationErrors);
      if (!CollectionUtils.isEmpty(validationIssues)) {
        log.warn("validateMarcRecord:: Validation failed with {} issues", validationIssues.size());
        throw new MarcRecordValidationException(
          new org.folio.qm.domain.dto.ValidationResult()
            .issues(validationIssues));
      }
      log.debug("validateMarcRecord:: Validation successful");
    } else {
      log.debug("validateMarcRecord:: Skipping validation for HOLDINGS format");
    }
  }

  @Override
  public void validateMarcRecord(QuickMarcRecord marcRecord, List<SkippedValidationError> skippedValidationErrors) {
    if (marcRecord.getMarcFormat() != MarcFormat.HOLDINGS) {
      log.debug("validateMarcRecord:: validate a quickMarc record with format: {}", marcRecord.getMarcFormat());

      var validationIssues = getValidationIssues(marcRecord.getMarcRecord(), skippedValidationErrors,
        marcRecord.getMarcFormat());
      if (!CollectionUtils.isEmpty(validationIssues)) {
        log.warn("validateMarcRecord:: Validation failed with {} issues", validationIssues.size());
        throw new MarcRecordValidationException(
          new org.folio.qm.domain.dto.ValidationResult()
            .issues(validationIssues));
      }
      log.debug("validateMarcRecord:: Validation successful");
    } else {
      log.debug("validateMarcRecord:: Skipping validation for HOLDINGS format");
    }
  }

  private List<ValidationIssue> getValidationIssues(Record validatableRecord,
                                                    List<SkippedValidationError> skippedValidationErrors,
                                                    MarcFormat marcFormat) {
    log.trace("getValidationIssues:: Validating MARC record with format: {}", marcFormat);
    var specification = marcSpecificationService.getSpecification(marcFormat);
    return validatableRecordValidator.validate(validatableRecord, specification)
      .stream()
      .filter(validationError -> containsErrorSeverityType(validationError, skippedValidationErrors))
      .map(validationError -> toValidationIssue(validationError, specification))
      .toList();
  }

  private List<ValidationIssue> getValidationIssues(ValidatableRecord validatableRecord,
                                                    List<SkippedValidationError> skippedValidationErrors) {
    log.trace("getValidationIssues:: Validating validatable record with format: {}", validatableRecord.getMarcFormat());
    var specification = marcSpecificationService.getSpecification(validatableRecord.getMarcFormat());
    return validatableRecordValidator.validate(new ValidatableRecordDelegate(validatableRecord), specification)
      .stream()
      .filter(validationError -> containsErrorSeverityType(validationError, skippedValidationErrors))
      .map(validationError -> toValidationIssue(validationError, specification))
      .toList();
  }

  private boolean containsErrorSeverityType(
    ValidationError validationError, List<SkippedValidationError> skippedValidationErrors) {

    return validationError != null
           && SeverityType.ERROR.getType().equalsIgnoreCase(validationError.getSeverity().getType())
           && !isSkippedError(validationError, skippedValidationErrors);
  }

  private boolean isSkippedError(
    ValidationError validationError, List<SkippedValidationError> skippedValidationErrors) {

    return !CollectionUtils.isEmpty(skippedValidationErrors) && skippedValidationErrors.stream()
      .anyMatch(skippedValidationError ->
        validationError.getPath().matches(String.format(TAG_NAME_REGEX, skippedValidationError.tagName()))
        && validationError.getRuleCode().equals(skippedValidationError.ruleCode().getCode()));
  }

  private ValidationIssue toValidationIssue(ValidationError validationError, SpecificationDto specification) {
    var path = validationError.getPath();
    var helpUrl = SpecificationUtils.findField(specification, path.substring(0, path.indexOf('[')))
      .map(SpecificationFieldDto::getUrl)
      .orElse(null);
    var message = getValidationIssueMessage(validationError);
    return new ValidationIssue()
      .tag(path.substring(0, path.indexOf(']') + 1))
      .helpUrl(helpUrl)
      .severity(validationError.getSeverity().getType())
      .definitionType(validationError.getDefinitionType().getType())
      .message(message);
  }

  private String getValidationIssueMessage(ValidationError validationError) {
    var originalMessage = validationError.getMessage();
    var ruleCode = validationError.getRuleCode();
    return INVALID_INDICATOR.getCode().equals(ruleCode) || UNDEFINED_INDICATOR.getCode().equals(ruleCode)
           ? originalMessage.replace('#', '\\')
           : originalMessage;
  }
}
