package org.folio.qm.service.impl;

import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.util.ErrorUtils.buildError;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.converter.QuickMarcRecordConverter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.validation.SkippedValidationError;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class MarcRecordsServiceImpl implements MarcRecordsService {

  public static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "Request id and entity id are not equal";

  private final ValidationService validationService;
  private final MarcRecordServiceRegistry marcRecordServiceRegistry;

  private final Converter<QuickMarcCreate, QuickMarcRecord> quickMarcCreateQuickMarcRecordConverter;
  private final Converter<QuickMarcEdit, QuickMarcRecord> quickMarcEditQuickMarcRecordConverter;
  private final QuickMarcRecordConverter quickMarcRecordConverter;

  @Override
  public void updateById(UUID parsedRecordId, QuickMarcEdit quickMarc) {
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", parsedRecordId);
    validateIdsMatch(quickMarc, parsedRecordId);
    var quickMarcRecord = quickMarcEditQuickMarcRecordConverter.convert(quickMarc);
    validateOnUpdate(quickMarc);
    var recordService = marcRecordServiceRegistry.get(quickMarc.getMarcFormat());
    recordService.update(quickMarcRecord);
    log.info("updateById:: quickMarc updated by parsedRecordId: {}", parsedRecordId);
  }

  @Override
  public QuickMarcView createRecord(QuickMarcCreate quickMarc) {
    log.debug("createRecord:: trying to create a new quickMarc");

    var quickMarcRecord = quickMarcCreateQuickMarcRecordConverter.convert(quickMarc);

    validateOnCreate(quickMarc);
    var recordService = marcRecordServiceRegistry.get(quickMarc.getMarcFormat());
    recordService.create(quickMarcRecord);
    log.info("createRecord:: new quickMarc created with qmRecordId: {}", quickMarcRecord.getExternalId());
    return quickMarcRecordConverter.convert(quickMarcRecord);
  }

  private void validateOnCreate(BaseMarcRecord quickMarc) {
    var skippedValidationError = new SkippedValidationError(TAG_001_CONTROL_FIELD, MarcRuleCode.MISSING_FIELD);
    validationService.validateMarcRecord(quickMarc, List.of(skippedValidationError));
    validateMarcRecord(quickMarc);
  }

  private void validateOnUpdate(BaseMarcRecord quickMarc) {
    validationService.validateMarcRecord(quickMarc, Collections.emptyList());
    validateMarcRecord(quickMarc);
  }

  private void validateMarcRecord(BaseMarcRecord marcRecord) {
    var validationResult = validationService.validate(marcRecord);
    if (!validationResult.isValid()) {
      throw new FieldsValidationException(validationResult);
    }
  }

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
