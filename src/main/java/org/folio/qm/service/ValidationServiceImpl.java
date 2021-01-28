package org.folio.qm.service;

import static org.folio.qm.util.ErrorUtils.buildError;

import java.util.UUID;

import org.springframework.stereotype.Service;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.util.ErrorUtils;

@Service
public class ValidationServiceImpl implements ValidationService {

  public static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "request id and entity id are not equal";

  @Override
  public void validateIds(QuickMarc quickMarc, UUID instanceId) {
    if (!quickMarc.getParsedRecordId().equals(instanceId.toString())) {
      var error = buildError(400, ErrorUtils.ErrorType.INTERNAL, REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE);
      throw new ValidationException(error);
    }
  }
}
