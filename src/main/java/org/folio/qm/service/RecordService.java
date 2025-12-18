package org.folio.qm.service;

import static org.folio.qm.util.JsonUtils.objectToJsonString;

import io.vertx.core.json.JsonObject;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.AdditionalInfo;
import org.folio.ParsedRecord;
import org.folio.RawRecord;
import org.folio.Record;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapperBuilder;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.mapper.ExternalIdsHolderMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.util.ErrorUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

@Log4j2
public abstract class RecordService<T> {

  private final MappingMetadataProvider mappingMetadataProvider;
  private final SourceStorageClient sourceStorageClient;
  private final ExternalIdsHolderMapper externalIdsHolderMapper;

  protected RecordService(MappingMetadataProvider mappingMetadataProvider,
                          SourceStorageClient sourceStorageClient,
                          ExternalIdsHolderMapper externalIdsHolderMapper) {
    this.mappingMetadataProvider = mappingMetadataProvider;
    this.sourceStorageClient = sourceStorageClient;
    this.externalIdsHolderMapper = externalIdsHolderMapper;
  }

  public abstract RecordTypeEnum supportedType();

  public abstract void update(UUID parsedRecordId,
                              DeferredResult<ResponseEntity<Void>> updateResult,
                              ParsedRecordDto parsedRecordDto);

  protected T getMappedRecord(ParsedRecordDto parsedRecordDto, String recordType, String mapperName) {
    var mappingMetadata = mappingMetadataProvider.getMappingData(recordType);
    if (Objects.isNull(mappingMetadata)) {
      return null;
    }
    var parsedRecordJson = retrieveParsedContent(parsedRecordDto);
    RecordMapper<T> recordMapper = RecordMapperBuilder.buildMapper(mapperName);
    return recordMapper.mapRecord(
      parsedRecordJson,
      mappingMetadata.mappingParameters(),
      mappingMetadata.mappingRules());
  }

  protected JsonObject retrieveParsedContent(ParsedRecordDto parsedRecordDto) {
    var parsedRecordContent = parsedRecordDto.getParsedRecord().getContent();
    return parsedRecordContent instanceof String content ? new JsonObject(content) :
      JsonObject.mapFrom(parsedRecordContent);
  }

  protected void updateSrsRecord(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                                 ParsedRecordDto parsedRecordDto) {
    var existingRecord = sourceStorageClient.getSrsRecord(parsedRecordId.toString());
    if (existingRecord == null) {
      handleError(parsedRecordId, updateResult,
        String.format("updateSrsRecord:: existing SRS record not found for parsedRecordId: %s", parsedRecordId));
      return;
    }
    var srsRecord = getNewRecord(parsedRecordDto, existingRecord);
    sourceStorageClient.updateSrsRecordGeneration(srsRecord.getId(), srsRecord);
    log.debug("updateSrsRecord:: quickMarc update SRS record successful for parsedRecordId: {}", parsedRecordId);
    updateResult.setResult(ResponseEntity.accepted().build());
  }

  protected void handleError(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                             String errorMessage) {
    log.error("handleError:: {}", errorMessage);
    setErrorResult(parsedRecordId, updateResult, errorMessage);
  }

  protected void handleError(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                             String errorMessage, Exception e) {
    log.error("handleError:: {}", errorMessage, e);
    setErrorResult(parsedRecordId, updateResult, errorMessage);
  }

  private void setErrorResult(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                              String errorMessage) {
    var error = ErrorUtils.buildError(
      ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED,
      String.format("Failed to update MARC record for parsedRecordId: %s, error message: %s",
        parsedRecordId, errorMessage));
    updateResult.setErrorResult(ResponseEntity.badRequest().body(error));
  }

  private Record getNewRecord(ParsedRecordDto parsedRecordDto, Record existingRecord) {
    var newRecordId = parsedRecordDto.getId().toString();
    var externalIdsHolder = parsedRecordDto.getExternalIdsHolder();
    var metadata = existingRecord.getMetadata();
    return new Record()
      .withId(newRecordId)
      .withSnapshotId(existingRecord.getSnapshotId())
      .withMatchedId(parsedRecordDto.getId().toString())
      .withRecordType(Record.RecordType.fromValue(parsedRecordDto.getRecordType().getValue()))
      .withOrder(existingRecord.getOrder())
      .withDeleted(false)
      .withState(Record.State.ACTUAL)
      .withGeneration(existingRecord.getGeneration() + 1)
      .withRawRecord(toRawRecord(parsedRecordDto.getParsedRecord(), newRecordId))
      .withParsedRecord(new ParsedRecord()
        .withId(newRecordId)
        .withContent(parsedRecordDto.getParsedRecord().getContent()))
      .withExternalIdsHolder(externalIdsHolderMapper.map(externalIdsHolder))
      .withAdditionalInfo(new AdditionalInfo()
        .withSuppressDiscovery(parsedRecordDto.getAdditionalInfo().isSuppressDiscovery()))
      .withMetadata(metadata.withUpdatedDate(new Date()));
  }

  private RawRecord toRawRecord(org.folio.qm.client.model.ParsedRecord parsedRecord, String recordId) {
    var jsonString = objectToJsonString(parsedRecord.getContent());
    return new RawRecord()
      .withId(recordId)
      .withContent(jsonString);
  }
}
