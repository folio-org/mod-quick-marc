package org.folio.qm.service.impl;

import static org.folio.qm.util.ErrorUtils.buildError;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.ValidationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.validation.ValidationResult;
import org.folio.qm.validation.ValidationRule;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ValidationServiceImpl implements ValidationService {

  public static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "Request id and entity id are not equal";

  private final List<ValidationRule> validationRules;

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
  public void validateIdsMatch(QuickMarcEdit quickMarc, UUID parsedRecordId) {
    if (!quickMarc.getParsedRecordId().equals(parsedRecordId)) {
      log.warn("validateIdsMatch:: request id: {} and entity id: {} are not equal",
        quickMarc.getParsedRecordId(), parsedRecordId);
      var error =
        buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE);
      throw new ValidationException(error);
    }
  }

}
