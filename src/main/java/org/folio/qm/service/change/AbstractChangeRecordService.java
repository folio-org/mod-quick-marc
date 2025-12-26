package org.folio.qm.service.change;

import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
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
import org.folio.qm.converter.QuickMarcRecordConverter;
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
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.SkippedValidationError;
import org.folio.qm.service.validation.ValidationService;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.util.MarcRecordModifier;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;

@Log4j2
public abstract class AbstractChangeRecordService<T extends FolioRecord> implements ChangeRecordService {

  public static final String REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE = "Request id and entity id are not equal";

  private final ValidationService validationService;
  private final Converter<QuickMarcCreate, QuickMarcRecord> quickMarcCreateQuickMarcRecordConverter;
  private final Converter<QuickMarcEdit, QuickMarcRecord> quickMarcEditQuickMarcRecordConverter;
  private final QuickMarcRecordConverter quickMarcRecordConverter;
  private final MarcMappingService<T> marcMappingService;
  private final SourceRecordService sourceRecordService;

  protected AbstractChangeRecordService(ValidationService validationService,
                                        Converter<QuickMarcCreate, QuickMarcRecord> quickMarcCreateQuickMarcRecordConverter,
                                        Converter<QuickMarcEdit, QuickMarcRecord> quickMarcEditQuickMarcRecordConverter,
                                        QuickMarcRecordConverter quickMarcRecordConverter,
                                        SourceRecordService sourceRecordService,
                                        MarcMappingService<T> marcMappingService) {
    this.validationService = validationService;
    this.quickMarcCreateQuickMarcRecordConverter = quickMarcCreateQuickMarcRecordConverter;
    this.quickMarcEditQuickMarcRecordConverter = quickMarcEditQuickMarcRecordConverter;
    this.quickMarcRecordConverter = quickMarcRecordConverter;
    this.marcMappingService = marcMappingService;
    this.sourceRecordService = sourceRecordService;
  }

  @Override
  public void update(UUID recordId, QuickMarcEdit qmEditRecord) {
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", recordId);
    validateIdsMatch(qmEditRecord, recordId);
    var quickMarcRecord = quickMarcEditQuickMarcRecordConverter.convert(qmEditRecord);
    validateOnUpdate(qmEditRecord);
    update(quickMarcRecord);
    log.info("updateById:: quickMarc updated by parsedRecordId: {}", recordId);
  }

  @Override
  public QuickMarcView create(QuickMarcCreate qmCreateRecord) {
    log.debug("createRecord:: trying to create a new quickMarc");

    var quickMarcRecord = quickMarcCreateQuickMarcRecordConverter.convert(qmCreateRecord);

    validateOnCreate(qmCreateRecord);
    create(quickMarcRecord);
    log.info("createRecord:: new quickMarc created with qmRecordId: {}", quickMarcRecord.getExternalId());
    return quickMarcRecordConverter.convert(quickMarcRecord);
  }

  public abstract ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord);

  public void validateIdsMatch(QuickMarcEdit quickMarc, UUID parsedRecordId) {
    if (!quickMarc.getParsedRecordId().equals(parsedRecordId)) {
      log.warn("validateIdsMatch:: request id: {} and entity id: {} are not equal",
        quickMarc.getParsedRecordId(), parsedRecordId);
      var error =
        buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, REQUEST_AND_ENTITY_ID_NOT_EQUAL_MESSAGE);
      throw new ValidationException(error);
    }
  }

  protected abstract void update(QuickMarcRecord quickMarcRecord);

  protected abstract void create(QuickMarcRecord quickMarcRecord);

  protected T getMappedRecord(QuickMarcRecord qmRecord) {
    return marcMappingService.mapNewRecord(qmRecord);
  }

  protected T getMappedRecord(QuickMarcRecord qmRecord, T existingRecord) {
    return marcMappingService.mapUpdatedRecord(qmRecord, existingRecord);
  }

  protected void updateSrsRecord(QuickMarcRecord qmRecord) {
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

  protected void createSrsRecord(QuickMarcRecord qmRecord, boolean add001Field) {
    // Step 2: Modify MARC record to add required fields
    addRequiredFieldsToMarcRecord(qmRecord, add001Field);

    // Step 3: Build SRS record with snapshot ID
    var srsRecord = buildNewSrsRecord(qmRecord);

    // Step 4: Create SRS record
    var createdRecord = sourceRecordService.create(srsRecord);

    // Step 5: Update QuickMarcRecord with generated IDs
    qmRecord.setParsedRecordId(UUID.fromString(createdRecord.getParsedRecord().getId()));
    qmRecord.setParsedRecordDtoId(UUID.fromString(createdRecord.getId()));
  }

  private void addRequiredFieldsToMarcRecord(QuickMarcRecord qmRecord, boolean add001Field) {
    try {
      // Parse existing MARC record from ParsedContent
      org.marc4j.marc.Record marcRecord = qmRecord.getMarcRecord();

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
}
