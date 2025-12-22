package org.folio.qm.service.impl;

import static org.folio.qm.util.DataImportEventUtils.FolioRecord.MARC_HOLDINGS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.ExternalIdsHolder;
import org.folio.Holdings;
import org.folio.qm.client.HoldingsStorageClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.converter.MarcQmConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.mapper.HoldingsRecordMapper;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.service.RecordService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.folio.rest.jaxrs.model.Metadata;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class HoldingRecordService extends RecordService<Holdings> {

  private final HoldingsStorageClient holdingsStorageClient;
  private final FolioExecutionContext folioExecutionContext;
  private final HoldingsRecordMapper mapper;

  public HoldingRecordService(MappingMetadataProvider mappingMetadataProvider,
                              SourceStorageClient sourceStorageClient,
                              MarcQmConverter<QuickMarcEdit> marcQmConverter,
                              MarcTypeMapper typeMapper,
                              HoldingsStorageClient holdingsStorageClient,
                              FolioExecutionContext folioExecutionContext,
                              HoldingsRecordMapper mapper) {
    super(mappingMetadataProvider, sourceStorageClient, marcQmConverter, typeMapper);
    this.holdingsStorageClient = holdingsStorageClient;
    this.folioExecutionContext = folioExecutionContext;
    this.mapper = mapper;
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.HOLDINGS;
  }

  @Override
  public void update(QuickMarcEdit quickMarc) {
    updateSrsRecord(quickMarc);
    Holdings mappedHolding = getMappedRecord(quickMarc);
    var holdingId = quickMarc.getExternalId().toString();
    var holding = holdingsStorageClient.getHoldingById(holdingId);
    if (holding == null) {
      throw new NotFoundException(String.format("Holdings record with id: %s not found", holdingId));
    }
    updateHolding(holding, mappedHolding);
  }

  @Override
  public ExternalIdsHolder getExternalIdsHolder(QuickMarcEdit quickMarc) {
    return new ExternalIdsHolder()
      .withHoldingsId(quickMarc.getExternalId().toString())
      .withHoldingsHrid(quickMarc.getExternalHrid());
  }

  @Override
  public MappingRecordTypeEnum getMapperRecordType() {
    return  MappingRecordTypeEnum.MARC_HOLDINGS;
  }

  @Override
  public String getMapperName() {
    return MARC_HOLDINGS.name();
  }

  private void updateHolding(HoldingsRecord holding, Holdings mappedHolding) {
    var holdingRecord = convertToHoldingsRecord(holding, mappedHolding);
    holdingsStorageClient.updateHolding(holdingRecord.getId(), holdingRecord);
    log.debug("Holding record with id: {} has been updated successfully", holdingRecord.getId());
  }

  private HoldingsRecord convertToHoldingsRecord(HoldingsRecord existingRecord, Holdings mappedRecord) {
    mappedRecord.setId(existingRecord.getId());
    mappedRecord.setVersion(existingRecord.getVersion() != null ? existingRecord.getVersion().intValue() : null);
    var statisticalCodeIds = new HashSet<>(Optional.ofNullable(existingRecord.getStatisticalCodeIds())
      .orElse(Collections.emptySet()));
    var administrativeNotes = new ArrayList<>(Optional.ofNullable(existingRecord.getAdministrativeNotes())
      .orElse(Collections.emptyList()));
    mapper.merge(mappedRecord, existingRecord);
    existingRecord.setStatisticalCodeIds(statisticalCodeIds);
    existingRecord.setAdministrativeNotes(administrativeNotes);
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
    var userId = folioExecutionContext.getUserId();
    if (userId != null && StringUtils.isNotBlank(userId.toString())) {
      return userId.toString();
    }
    return null;
  }
}
