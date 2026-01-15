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
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
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
    log.debug("updateById:: trying to update record by id: {}", recordId);
    defaultValuesPopulationService.populate(qmEditRecord);
    validateIdsMatch(qmEditRecord, recordId);
    validateOnUpdate(qmEditRecord);

    var qmRecord = conversionService.convert(qmEditRecord, QuickMarcRecord.class);
    updateSourceRecord(qmRecord);
    updateFolioRecord(qmRecord);
    postProcess(qmRecord);
    log.info("updateById:: quickMarc updated by id: {}", recordId);
  }

  @Override
  public QuickMarcView create(QuickMarcCreate qmCreateRecord) {
    log.debug("createRecord:: trying to create a new record");
    defaultValuesPopulationService.populate(qmCreateRecord);
    validateOnCreate(qmCreateRecord);

    var qmRecord = conversionService.convert(qmCreateRecord, QuickMarcRecord.class);
    // rawContent should be without adding required fields and updating non-required fields
    qmRecord.setRawContent(qmRecord.getParsedContent().encode());
    createFolioRecord(qmRecord);
    createSourceRecord(qmRecord);
    log.info("createRecord:: new record created with externalId: {}", qmRecord.getExternalId());
    return conversionService.convert(qmRecord, QuickMarcView.class);
  }

  protected void postProcess(QuickMarcRecord qmRecord) {
    log.debug("postProcess:: Post processing of record with externalId: {}", qmRecord.getExternalId());
  }

  protected void updateNonRequiredFields(QuickMarcRecord qmRecord){
    // No-op by default
  }

  protected abstract ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord);

  protected abstract boolean adding001FieldRequired();

  private T getMappedRecord(QuickMarcRecord qmRecord) {
    return marcMappingService.mapNewRecord(qmRecord);
  }

  private T getMappedRecord(QuickMarcRecord qmRecord, T existingRecord) {
    return marcMappingService.mapUpdatedRecord(qmRecord, existingRecord);
  }

  private void updateSourceRecord(QuickMarcRecord qmRecord) {
    var recordId = qmRecord.getParsedRecordId();
    log.debug("updateSourceRecord:: Updating source record for parsedRecordId: {}", recordId);
    var existingRecord = sourceRecordService.get(recordId);
    validateRecordVersion(qmRecord, existingRecord);
    var sourceRecord = buildUpdatedRecord(qmRecord, existingRecord);
    sourceRecordService.update(UUID.fromString(sourceRecord.getMatchedId()), sourceRecord);
    log.debug("updateSourceRecord:: Source record updated successfully for parsedRecordId: {}",
      qmRecord.getParsedRecordDtoId());
  }

  private void createSourceRecord(QuickMarcRecord qmRecord) {
    log.debug("createSourceRecord:: Creating source record");
    addRequiredFieldsToMarcRecord(qmRecord, adding001FieldRequired());
    var sourceRecord = buildNewSourceRecord(qmRecord);

    var createdRecord = sourceRecordService.create(sourceRecord);

    qmRecord.setParsedRecordId(UUID.fromString(createdRecord.getParsedRecord().getId()));
    qmRecord.setParsedRecordDtoId(UUID.fromString(createdRecord.getId()));
    log.debug("createSrsRecord:: Source record created successfully with parsedRecordId: {}",
      qmRecord.getParsedRecordId());
  }

  private void updateFolioRecord(QuickMarcRecord qmRecord) {
    var externalId = qmRecord.getExternalId();
    log.debug("updateFolioRecord:: Updating folio record with id: {}", externalId);
    var existingRecord = folioRecordService.get(externalId);
    var mappedRecord = getMappedRecord(qmRecord, existingRecord);
    folioRecordService.update(externalId, mappedRecord);
    log.debug("updateFolioRecord:: Folio record updated successfully for id: {}", externalId);
  }

  private void createFolioRecord(QuickMarcRecord qmRecord) {
    log.debug("createFolioRecord:: Creating folio record");
    updateNonRequiredFields(qmRecord);
    var mappedRecord = getMappedRecord(qmRecord);
    var createdRecord = folioRecordService.create(mappedRecord);
    qmRecord.setExternalId(UUID.fromString(createdRecord.getId()));
    qmRecord.setExternalHrid(createdRecord.getHrid());
    qmRecord.setFolioRecord(createdRecord);
    log.debug("createFolioRecord:: Folio record created successfully with id: {}", qmRecord.getExternalId());
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

  private Record buildNewSourceRecord(QuickMarcRecord qmRecord) {
    var recordId = UUID.randomUUID().toString();

    return new Record()
      .withId(recordId)
      .withMatchedId(recordId)
      .withRecordType(qmRecord.getSourceRecordType())
      .withOrder(0)
      .withDeleted(false)
      .withState(Record.State.ACTUAL)
      .withGeneration(0)
      .withRawRecord(new RawRecord()
        .withId(recordId)
        .withContent(qmRecord.getRawContent()))
      .withParsedRecord(new ParsedRecord()
        .withId(recordId)
        .withContent(qmRecord.getParsedContent().encode()))
      .withExternalIdsHolder(getExternalIdsHolder(qmRecord))
      .withAdditionalInfo(new AdditionalInfo()
        .withSuppressDiscovery(qmRecord.isSuppressDiscovery()));
  }

  private Record buildUpdatedRecord(QuickMarcRecord qmRecord, Record existingRecord) {
    var recordId = qmRecord.getParsedRecordDtoId().toString();
    return new Record()
      .withId(recordId)
      .withSnapshotId(existingRecord.getSnapshotId())
      .withMatchedId(existingRecord.getMatchedId())
      .withRecordType(qmRecord.getSourceRecordType())
      .withOrder(existingRecord.getOrder())
      .withDeleted(false)
      .withState(Record.State.ACTUAL)
      .withGeneration(existingRecord.getGeneration() + 1)
      .withRawRecord(existingRecord.getRawRecord().withId(recordId))
      .withParsedRecord(new ParsedRecord()
        .withId(recordId)
        .withContent(qmRecord.getParsedContent().encode()))  // Use precomputed ParsedContent
      .withExternalIdsHolder(getExternalIdsHolder(qmRecord))
      .withAdditionalInfo(new AdditionalInfo()
        .withSuppressDiscovery(qmRecord.isSuppressDiscovery()));
  }

  private void validateOnCreate(BaseQuickMarcRecord quickMarc) {
    log.debug("validateOnCreate:: Validating MARC record on create");
    var skippedValidationError = new SkippedValidationError(TAG_001_CONTROL_FIELD, MarcRuleCode.MISSING_FIELD);
    validationService.validateMarcRecord(quickMarc, List.of(skippedValidationError));
    validateMarcRecord(quickMarc);
    log.debug("validateOnCreate:: Validation successful");
  }

  private void validateOnUpdate(BaseQuickMarcRecord quickMarc) {
    log.debug("validateOnUpdate:: Validating MARC record on update");
    validationService.validateMarcRecord(quickMarc, Collections.emptyList());
    validateMarcRecord(quickMarc);
    log.debug("validateOnUpdate:: Validation successful");
  }

  private void validateMarcRecord(BaseQuickMarcRecord marcRecord) {
    log.trace("validateMarcRecord:: Running custom validation rules");
    var validationResult = validationService.validate(marcRecord);
    if (!validationResult.isValid()) {
      log.warn("validateMarcRecord:: Validation failed with {} errors", validationResult.errors().size());
      throw new FieldsValidationException(validationResult);
    }
  }

  private void validateRecordVersion(QuickMarcRecord qmRecord, Record existingRecord) {
    var storedVersion = existingRecord.getGeneration();
    var recordId = qmRecord.getParsedRecordId();
    if (qmRecord.getSourceVersion() != null && !qmRecord.getSourceVersion().equals(storedVersion)) {
      log.warn("updateSourceRecord:: Version mismatch for recordId: {}. Expected: {}, Actual: {}",
        recordId, qmRecord.getSourceVersion(), storedVersion);
      throw new OptimisticLockingException(recordId, storedVersion, qmRecord.getSourceVersion());
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
