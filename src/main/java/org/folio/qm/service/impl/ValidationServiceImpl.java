package org.folio.qm.service.impl;

import static org.folio.qm.util.ErrorUtils.buildError;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.INVALID_INDICATOR;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.converter.MarcQmToValidatableRecordConverter;
import org.folio.qm.domain.ValidatableRecordDelegate;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidationIssue;
import org.folio.qm.exception.MarcRecordValidationException;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.MarcSpecificationService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.validation.ValidationResult;
import org.folio.qm.validation.ValidationRule;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.utils.SpecificationUtils;
import org.folio.rspec.validation.SpecificationGuidedValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Log4j2
public class ValidationServiceImpl implements ValidationService {

  public static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "Request id and entity id are not equal";

  private final List<ValidationRule> validationRules;
  private final MarcSpecificationService marcSpecificationService;
  private final SpecificationGuidedValidator validatableRecordValidator;
  private final MarcQmToValidatableRecordConverter converter;

  @Override
  public ValidationResult validate(BaseMarcRecord quickMarc) {
    var validationErrors = validationRules.stream()
      .filter(rule -> rule.supportFormat(quickMarc.getMarcFormat()))
      .map(rule -> rule.validate(quickMarc))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .toList();

    if (validationErrors.isEmpty()) {
      return new ValidationResult(true, Collections.emptyList());
    } else {
      return new ValidationResult(false, validationErrors);
    }
  }

  @Override
  public List<ValidationIssue> validate(ValidatableRecord validatableRecord) {
    var specification = marcSpecificationService.getSpecification(validatableRecord.getMarcFormat());
    return validatableRecordValidator.validate(new ValidatableRecordDelegate(validatableRecord), specification)
      .stream()
      .map(validationError -> toValidationIssue(validationError, specification))
      .toList();
  }

  @Override
  public void validateIdsMatch(QuickMarcEdit quickMarc, UUID parsedRecordId) {
    if (!quickMarc.getParsedRecordId().equals(parsedRecordId)) {
      log.warn("validateIdsMatch:: request id: {} and entity id: {} are not equal",
        quickMarc.getParsedRecordId(), parsedRecordId);
      var error =
        buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE);
      throw new ValidationException(error);
    }
  }

  @Override
  public void validateMarcRecord(BaseMarcRecord marcRecord) {
    if (marcRecord.getMarcFormat() != MarcFormat.HOLDINGS) {
      log.debug("validateMarcRecord:: validate a quickMarc record");
      var validatableRecord = converter.convert(marcRecord);
      var validationIssues = validate(validatableRecord);
      if (containsErrorSeverityType(validationIssues)) {
        throw new MarcRecordValidationException(
          new org.folio.qm.domain.dto.ValidationResult().issues(validationIssues));
      }
    }
  }

  private boolean containsErrorSeverityType(List<ValidationIssue> validationIssues) {
    return !CollectionUtils.isEmpty(validationIssues) && validationIssues.stream()
      .anyMatch(issue ->
        issue.getSeverity() != null && issue.getSeverity().equalsIgnoreCase(SeverityType.ERROR.getType()));
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
    return INVALID_INDICATOR.getCode().equals(validationError.getRuleCode())
      ? originalMessage.replace('#', '\\')
      : originalMessage;
  }
}
