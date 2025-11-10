package org.folio.qm.service.impl;

import static org.folio.qm.util.DataImportEventUtils.FolioRecord.MARC_HOLDINGS;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.Holdings;
import org.folio.Metadata;
import org.folio.qm.client.HoldingsStorageClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.domain.entity.HoldingsRecord;
import org.folio.qm.mapper.ExternalIdsHolderMapper;
import org.folio.qm.mapper.HoldingsRecordMapper;
import org.folio.qm.service.RecordService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.spring.FolioExecutionContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@Log4j2
public class HoldingRecordService extends RecordService<Holdings> {

  private final HoldingsStorageClient holdingsStorageClient;
  private final FolioExecutionContext folioExecutionContext;
  private final HoldingsRecordMapper mapper;

  public HoldingRecordService(MappingMetadataProvider mappingMetadataProvider,
                              SourceStorageClient sourceStorageClient,
                              ExternalIdsHolderMapper externalIdsHolderMapper,
                              HoldingsStorageClient holdingsStorageClient,
                              FolioExecutionContext folioExecutionContext,
                              HoldingsRecordMapper mapper) {
    super(mappingMetadataProvider, sourceStorageClient, externalIdsHolderMapper);
    this.holdingsStorageClient = holdingsStorageClient;
    this.folioExecutionContext = folioExecutionContext;
    this.mapper = mapper;
  }

  @Override
  public RecordTypeEnum supportedType() {
    return RecordTypeEnum.HOLDING;
  }

  @Override
  public void update(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                     ParsedRecordDto parsedRecordDto) {
    try {
      Holdings mappedHolding = getMappedRecord(parsedRecordDto, MappingRecordTypeEnum.MARC_HOLDINGS.getValue(),
        MARC_HOLDINGS.name());
      if (mappedHolding == null) {
        handleError(parsedRecordDto.getId(), updateResult,
          String.format("getMappedRecord:: mapping metadata not found for Holding record with parsedRecordId: %s",
            parsedRecordId));
        return;
      }
      var holdingId = parsedRecordDto.getExternalIdsHolder().getHoldingsId().toString();
      var holding = holdingsStorageClient.getHoldingById(holdingId);
      if (holding == null) {
        handleError(parsedRecordId, updateResult, String.format("Holding record with id %s was not found", holdingId));
        return;
      }
      updateHolding(holding, mappedHolding);
      updateSrsRecord(parsedRecordId, updateResult, parsedRecordDto);
    } catch (Exception e) {
      handleError(parsedRecordId, updateResult,
        String.format("Error updating holding record for parsedRecordId: %s, error: %s",
          parsedRecordId, e.getMessage()), e);
    }
  }

  private void updateHolding(HoldingsRecord holding, Holdings mappedHolding) {
    var holdingRecord = convertToHoldingsRecord(holding, mappedHolding);
    holdingsStorageClient.updateHolding(holdingRecord.getId(), holdingRecord);
    log.debug("Holding record with id: {} has been updated successfully", holdingRecord.getId());
  }

  private HoldingsRecord convertToHoldingsRecord(HoldingsRecord existingRecord, Holdings mappedRecord) {
    mappedRecord.setId(existingRecord.getId());
    mappedRecord.setVersion(existingRecord.getVersion());
    mapper.merge(mappedRecord, existingRecord);
    return populateUpdatedByUserIdIfNeeded(existingRecord);
  }

  private HoldingsRecord populateUpdatedByUserIdIfNeeded(HoldingsRecord holding) {
    if (holding.getMetadata() == null) {
      holding.setMetadata(new Metadata());
    }
    if (StringUtils.isBlank(holding.getMetadata().getUpdatedByUserId())) {
      holding.getMetadata().setUpdatedByUserId(getUserId());
    }
    return holding;
  }

  private String getUserId() {
    if (StringUtils.isNotBlank(folioExecutionContext.getUserId().toString())) {
      return folioExecutionContext.getUserId().toString();
    }
    return null;
  }
}
