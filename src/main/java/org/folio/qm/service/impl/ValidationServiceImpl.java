package org.folio.qm.service.impl;

import static org.folio.qm.util.ErrorUtils.buildError;

import java.util.Objects;
import java.util.UUID;

import org.folio.qm.domain.dto.QuickMarcFields;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.ValidationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.FolioExecutionContext;

@Service
public class ValidationServiceImpl implements ValidationService {

  public static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "Request id and entity id are not equal";
  public static final String QM_RECORD_ID_EMPTY_MESSAGE = "Parameter 'qmRecordId' should be not null";
  public static final String X_OKAPI_TOKEN_USER_ID_IS_MISSING_MESSAGE = "X-Okapi-User-Id header is missing";
  public static final String INVALID_MARC_TAG_MESSAGE = "Invalid MARC tag.The tag has alphabetic symbols";
  private static final String TAG_PATTERN = "\\d{0,3}";

  @Override
  public void validateIdsMatch(QuickMarc quickMarc, UUID instanceId) {
    if (!quickMarc.getParsedRecordId().equals(instanceId.toString())) {
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

  @Override
  public void validateUserId(FolioExecutionContext folioExecutionContext) {
    if (Objects.isNull(folioExecutionContext.getUserId())) {
      var error = buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, X_OKAPI_TOKEN_USER_ID_IS_MISSING_MESSAGE);
      throw new ValidationException(error);
    }
  }

  @Override
  public void validateTags(QuickMarc quickMarc) {
    boolean isInvalidTag = quickMarc.getFields().stream()
      .map(QuickMarcFields::getTag)
      .anyMatch(tag -> !tag.matches(TAG_PATTERN));
    if (isInvalidTag) {
      var error = buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, INVALID_MARC_TAG_MESSAGE);
      throw new ValidationException(error);
    }
  }
}
