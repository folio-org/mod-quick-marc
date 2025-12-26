package org.folio.qm.service.change;

import static org.folio.qm.convertion.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.util.ErrorUtils.buildError;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.AdditionalInfo;
import org.folio.ExternalIdsHolder;
import org.folio.ParsedRecord;
import org.folio.RawRecord;
import org.folio.Record;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.FolioRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.OptimisticLockingException;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.mapping.MarcMappingService;
import org.folio.qm.service.population.DefaultValuesPopulationService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.SkippedValidationError;
import org.folio.qm.service.validation.ValidationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.util.MarcRecordModifier;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.springframework.http.HttpStatus;

@Log4j2
public abstract class AbstractChangeRecordService<T extends FolioRecord> implements ChangeRecordService {

  private static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "Request id and entity id are not equal";

  private final ValidationService validationService;
  private final RecordConversionService conversionService;
  private final MarcMappingService<T> marcMappingService;
  private final SourceRecordService sourceRecordService;
  private final FolioRecordService<T> folioRecordService;
  private final DefaultValuesPopulationService defaultValuesPopulationService;

  protected AbstractChangeRecordService(ValidationService validationService,
                                        RecordConversionService conversionService,
                                        SourceRecordService sourceRecordService,
                                        MarcMappingService<T> marcMappingService,
                                        FolioRecordService<T> folioRecordService,
                                        DefaultValuesPopulationService defaultValuesPopulationService) {
    this.validationService = validationService;
    this.conversionService = conversionService;
    this.marcMappingService = marcMappingService;
    this.sourceRecordService = sourceRecordService;
    this.folioRecordService = folioRecordService;
    this.defaultValuesPopulationService = defaultValuesPopulationService;
  }

  @Override
  public void update(UUID recordId, QuickMarcEdit qmEditRecord) {
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", recordId);
    defaultValuesPopulationService.populate(qmEditRecord);
    validateIdsMatch(qmEditRecord, recordId);
    validateOnUpdate(qmEditRecord);

    var quickMarcRecord = conversionService.convert(qmEditRecord, QuickMarcRecord.class);
    updateSrsRecord(quickMarcRecord);
    updateFolioRecord(quickMarcRecord);
    postProcess(quickMarcRecord);
    log.info("updateById:: quickMarc updated by parsedRecordId: {}", recordId);
  }

  @Override
  public QuickMarcView create(QuickMarcCreate qmCreateRecord) {
    log.debug("createRecord:: trying to create a new quickMarc");
    defaultValuesPopulationService.populate(qmCreateRecord);
    validateOnCreate(qmCreateRecord);

    var quickMarcRecord = conversionService.convert(qmCreateRecord, QuickMarcRecord.class);
    createFolioRecord(quickMarcRecord);
    createSrsRecord(quickMarcRecord);
    log.info("createRecord:: new quickMarc created with qmRecordId: {}", quickMarcRecord.getExternalId());
    return conversionService.convert(quickMarcRecord, QuickMarcView.class);
  }

  protected void postProcess(QuickMarcRecord qmRecord) {
    log.debug("postProcess:: Post processing of record with externalId: {}", qmRecord.getExternalId());
  }

  protected abstract ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord);

  protected abstract boolean adding001FieldRequired();

  private void createFolioRecord(QuickMarcRecord qmRecord) {
    var mappedRecord = getMappedRecord(qmRecord);
    var createdRecord = folioRecordService.create(mappedRecord);
    qmRecord.setExternalId(UUID.fromString(createdRecord.getId()));
    qmRecord.setExternalHrid(createdRecord.getHrid());
    qmRecord.setFolioRecord(createdRecord);
  }

  private T getMappedRecord(QuickMarcRecord qmRecord) {
    return marcMappingService.mapNewRecord(qmRecord);
  }

  private T getMappedRecord(QuickMarcRecord qmRecord, T existingRecord) {
    return marcMappingService.mapUpdatedRecord(qmRecord, existingRecord);
  }

  private void updateSrsRecord(QuickMarcRecord qmRecord) {
    var recordId = qmRecord.getParsedRecordId();
    var existingRecord = sourceRecordService.get(recordId);
    var storedVersion = existingRecord.getGeneration();
    if (qmRecord.getSourceVersion() != null && !qmRecord.getSourceVersion().equals(storedVersion)) {
      throw new OptimisticLockingException(recordId, storedVersion, qmRecord.getSourceVersion());
    }
    var srsRecord = buildUpdatedRecord(qmRecord, existingRecord);
    sourceRecordService.update(recordId, srsRecord);
    log.debug("updateSrsRecord:: quickMarc update SRS record successful for parsedRecordId: {}",
      qmRecord.getParsedRecordDtoId());
  }

  private void createSrsRecord(QuickMarcRecord qmRecord) {
    addRequiredFieldsToMarcRecord(qmRecord, adding001FieldRequired());
    var srsRecord = buildNewSrsRecord(qmRecord);

    var createdRecord = sourceRecordService.create(srsRecord);

    qmRecord.setParsedRecordId(UUID.fromString(createdRecord.getParsedRecord().getId()));
    qmRecord.setParsedRecordDtoId(UUID.fromString(createdRecord.getId()));
  }

  private void updateFolioRecord(QuickMarcRecord qmRecord) {
    var externalId = qmRecord.getExternalId();
    var existingRecord = folioRecordService.get(externalId);
    var mappedRecord = getMappedRecord(qmRecord, existingRecord);
    folioRecordService.update(externalId, mappedRecord);
  }

  private void addRequiredFieldsToMarcRecord(QuickMarcRecord qmRecord, boolean add001Field) {
    try {
      var marcRecord = qmRecord.getMarcRecord();

      // Add 999 field with external ID in subfield $i
      if (qmRecord.getExternalId() != null) {
        MarcRecordModifier.add999Field(marcRecord, qmRecord.getExternalId().toString());
      }

      // Add 001 field with HRID (for Holdings and Instance only)
      if (add001Field && qmRecord.getExternalHrid() != null) {
        MarcRecordModifier.add001Field(marcRecord, qmRecord.getExternalHrid());
      }
      qmRecord.buildParsedContent();
    } catch (Exception e) {
      log.error("addRequiredFieldsToMarcRecord:: Failed to add required fields", e);
      throw new IllegalStateException("Failed to add required fields to MARC record", e);
    }
  }

  private Record buildNewSrsRecord(QuickMarcRecord qmRecord) {
    var recordId = UUID.randomUUID().toString();

    return new Record()
      .withId(recordId)
      .withMatchedId(recordId)
      .withRecordType(qmRecord.getSrsRecordType())
      .withOrder(0)
      .withDeleted(false)
      .withState(Record.State.ACTUAL)
      .withGeneration(0)
      .withRawRecord(new RawRecord()
        .withId(recordId)
        .withContent(qmRecord.getParsedContent().encode()))
      .withParsedRecord(new ParsedRecord()
        .withId(recordId)
        .withContent(qmRecord.getParsedContent()))
      .withExternalIdsHolder(getExternalIdsHolder(qmRecord))
      .withAdditionalInfo(new AdditionalInfo()
        .withSuppressDiscovery(qmRecord.isSuppressDiscovery()));
  }

  private Record buildUpdatedRecord(QuickMarcRecord qmRecord, Record existingRecord) {
    var recordId = qmRecord.getParsedRecordDtoId().toString();
    return new Record()
      .withId(recordId)
      .withSnapshotId(existingRecord.getSnapshotId())
      .withMatchedId(recordId)
      .withRecordType(qmRecord.getSrsRecordType())
      .withOrder(existingRecord.getOrder())
      .withDeleted(false)
      .withState(Record.State.ACTUAL)
      .withGeneration(existingRecord.getGeneration() + 1)
      .withRawRecord(existingRecord.getRawRecord().withId(recordId))
      .withParsedRecord(new ParsedRecord()
        .withId(recordId)
        .withContent(qmRecord.getParsedContent()))  // Use precomputed ParsedContent
      .withExternalIdsHolder(getExternalIdsHolder(qmRecord))
      .withAdditionalInfo(new AdditionalInfo()
        .withSuppressDiscovery(qmRecord.isSuppressDiscovery()));
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

  private void validateIdsMatch(QuickMarcEdit quickMarc, UUID parsedRecordId) {
    if (!quickMarc.getParsedRecordId().equals(parsedRecordId)) {
      log.warn("validateIdsMatch:: request id: {} and entity id: {} are not equal",
        quickMarc.getParsedRecordId(), parsedRecordId);
      var error =
        buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE);
      throw new ValidationException(error);
    }
  }
}
