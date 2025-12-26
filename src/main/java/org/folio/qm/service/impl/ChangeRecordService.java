package org.folio.qm.service.impl;

import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_999_FIELD;

import java.util.UUID;
import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.AdditionalInfo;
import org.folio.ExternalIdsHolder;
import org.folio.ParsedRecord;
import org.folio.RawRecord;
import org.folio.Record;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.model.FolioRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.OptimisticLockingException;
import org.folio.qm.service.MarcMappingService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.util.MarcRecordModifier;

@Log4j2
public abstract class ChangeRecordService<T extends FolioRecord> {

  private static final Predicate<FieldItem> FIELD_001_PREDICATE =
    qmFields -> qmFields.getTag().equals(TAG_001_CONTROL_FIELD);
  private static final Predicate<FieldItem> FIELD_999_PREDICATE = qmFields -> qmFields.getTag().equals(TAG_999_FIELD);
  private static final Predicate<FieldItem> FIELD_EMPTY_PREDICATE = qmFields -> {
    final var content = qmFields.getContent();
    return content instanceof String stringContent && StringUtils.isEmpty(stringContent);
  };

  private final MarcMappingService<T> marcMappingService;
  private final SourceRecordService sourceRecordService;

  protected ChangeRecordService(SourceRecordService sourceRecordService, MarcMappingService<T> marcMappingService) {
    this.marcMappingService = marcMappingService;
    this.sourceRecordService = sourceRecordService;
  }

  public abstract void update(QuickMarcRecord qmRecord);

  public abstract void create(QuickMarcRecord qmRecord);

  public abstract MarcFormat supportedType();

  public abstract ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord);

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

  private QuickMarcCreate prepareRecord(QuickMarcCreate quickMarc) {
    var fieldItemPredicate = FIELD_EMPTY_PREDICATE.or(FIELD_999_PREDICATE);
    if (supportedType() != MarcFormat.AUTHORITY) {
      fieldItemPredicate = fieldItemPredicate.or(FIELD_001_PREDICATE);
    }
    quickMarc.getFields().removeIf(fieldItemPredicate);
    return quickMarc;
  }
}
