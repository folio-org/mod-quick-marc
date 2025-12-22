package org.folio.qm.service;

import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_999_FIELD;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.AdditionalInfo;
import org.folio.ExternalIdsHolder;
import org.folio.ParsedRecord;
import org.folio.Record;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapperBuilder;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.converter.MarcQmConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.exception.MappingMetadataException;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.spring.exception.NotFoundException;

@Log4j2
public abstract class RecordService<T> {

  private static final Predicate<FieldItem> FIELD_001_PREDICATE =
    qmFields -> qmFields.getTag().equals(TAG_001_CONTROL_FIELD);
  private static final Predicate<FieldItem> FIELD_999_PREDICATE = qmFields -> qmFields.getTag().equals(TAG_999_FIELD);
  private static final Predicate<FieldItem> FIELD_EMPTY_PREDICATE = qmFields -> {
    final var content = qmFields.getContent();
    return content instanceof String stringContent && StringUtils.isEmpty(stringContent);
  };

  private final MappingMetadataProvider mappingMetadataProvider;
  private final SourceStorageClient sourceStorageClient;
  private final MarcQmConverter<QuickMarcEdit> marcQmConverter;
  private final MarcTypeMapper typeMapper;

  protected RecordService(MappingMetadataProvider mappingMetadataProvider,
                          SourceStorageClient sourceStorageClient,
                          MarcQmConverter<QuickMarcEdit> marcQmConverter,
                          MarcTypeMapper typeMapper) {
    this.mappingMetadataProvider = mappingMetadataProvider;
    this.sourceStorageClient = sourceStorageClient;
    this.marcQmConverter = marcQmConverter;
    this.typeMapper = typeMapper;
  }

  public abstract MarcFormat supportedType();

  public abstract void update(QuickMarcEdit quickMarc);

  public abstract ExternalIdsHolder getExternalIdsHolder(QuickMarcEdit quickMarc);

  public abstract MappingRecordTypeEnum getMapperRecordType();

  public abstract String getMapperName();

  public QuickMarcView create(QuickMarcCreate quickMarc) {
    return null;
  }

  protected T getMappedRecord(QuickMarcEdit quickMarc) {
    var mappedRecordType = getMapperRecordType().getValue();
    try {
      var mappingMetadata = mappingMetadataProvider.getMappingData(mappedRecordType);
      if (Objects.isNull(mappingMetadata)) {
        throw new MappingMetadataException(
          String.format("mapping metadata not found for %s record with parsedRecordId: %s",
            mappedRecordType, quickMarc.getParsedRecordId()));
      }
      RecordMapper<T> recordMapper = RecordMapperBuilder.buildMapper(getMapperName());
      return recordMapper.mapRecord(
        retrieveParsedContent(quickMarc),
        mappingMetadata.mappingParameters(),
        mappingMetadata.mappingRules());
    } catch (MappingMetadataException e) {
      throw e;
    } catch (Exception e) {
      throw new MappingMetadataException(
        String.format("Error mapping %s record with parsedRecordId: %s", mappedRecordType,
          quickMarc.getParsedRecordId()), e);
    }
  }

  protected void updateSrsRecord(QuickMarcEdit quickMarc) {
    var existingRecord = sourceStorageClient.getSrsRecord(quickMarc.getParsedRecordId().toString());
    if (existingRecord == null) {
      throw new NotFoundException(String.format("The SRS record to update was not found for parsedRecordId: %s",
        quickMarc.getParsedRecordId()));
    }
    var srsRecord = getUpdatedRecord(quickMarc, existingRecord);
    sourceStorageClient.updateSrsRecordGeneration(srsRecord.getId(), srsRecord);
    log.debug("updateSrsRecord:: quickMarc update SRS record successful for parsedRecordId: {}",
      quickMarc.getParsedRecordDtoId());
  }

  private Record getUpdatedRecord(QuickMarcEdit quickMarc, Record existingRecord) {
    var recordId = quickMarc.getParsedRecordDtoId().toString();
    return new Record()
      .withId(recordId)
      .withSnapshotId(existingRecord.getSnapshotId())
      .withMatchedId(recordId)
      .withRecordType(Record.RecordType.fromValue(typeMapper.toDto(quickMarc.getMarcFormat()).getValue()))
      .withOrder(existingRecord.getOrder())
      .withDeleted(false)
      .withState(Record.State.ACTUAL)
      .withGeneration(existingRecord.getGeneration() + 1)
      .withRawRecord(existingRecord.getRawRecord().withId(recordId))
      .withParsedRecord(new ParsedRecord()
        .withId(recordId)
        .withContent(marcQmConverter.convertToParsedContent(quickMarc)))
      .withExternalIdsHolder(getExternalIdsHolder(quickMarc))
      .withAdditionalInfo(new AdditionalInfo()
        .withSuppressDiscovery(quickMarc.getSuppressDiscovery()));
  }

  private JsonObject retrieveParsedContent(QuickMarcEdit quickMarc) {
    var parsedContent = marcQmConverter.convertToParsedContent(quickMarc);
    return new JsonObject(parsedContent.toString());
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
