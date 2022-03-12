package org.folio.qm.service.impl;

import static org.folio.qm.util.ErrorUtils.buildError;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.ValidationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.validation.LeaderValidationRule;
import org.folio.qm.validation.RecordValidationRule;
import org.folio.qm.validation.ValidationResult;
import org.folio.qm.validation.ValidationRule;
import org.folio.spring.FolioExecutionContext;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

  public static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "Request id and entity id are not equal";
  public static final String QM_RECORD_ID_EMPTY_MESSAGE = "Parameter 'qmRecordId' should be not null";

  private final List<ValidationRule> validationRules;

  @Override
  public ValidationResult validate(QuickMarc quickMarc) {
    var validationErrors = validationRules.stream()
      .filter(rule -> rule.supportFormat(quickMarc.getMarcFormat()))
      .map(rule -> rule.validate(quickMarc))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());

    if (validationErrors.isEmpty()) {
      return new ValidationResult(true, Collections.emptyList());
    } else {
      return new ValidationResult(false, validationErrors);
    }
  }

  @Override
  public void validateIdsMatch(QuickMarc quickMarc, UUID parsedRecordId) {
    if (!quickMarc.getParsedRecordId().equals(parsedRecordId)) {
      var error = buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE);
      throw new ValidationException(error);
    }
  }

  @Override
  public void validateQmRecordId(UUID qmRecordId) {
    if (qmRecordId == null) {
      var error = buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, QM_RECORD_ID_EMPTY_MESSAGE);
      throw new ValidationException(error);
    }
  }

}
