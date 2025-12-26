package org.folio.qm.service.change;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.ExternalIdsHolder;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.HoldingsRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.mapping.MarcMappingService;
import org.folio.qm.service.population.DefaultValuesPopulationService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.ValidationService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class HoldingsChangeRecordService extends AbstractChangeRecordService<HoldingsRecord> {

  private final FolioRecordService<HoldingsRecord> folioRecordService;

  public HoldingsChangeRecordService(ValidationService validationService,
                                     RecordConversionService conversionService,
                                     SourceRecordService sourceRecordService,
                                     MarcMappingService<HoldingsRecord> mappingService,
                                     FolioRecordService<HoldingsRecord> folioRecordService,
                                     DefaultValuesPopulationService defaultValuesPopulationService) {
    super(validationService, conversionService, sourceRecordService, mappingService, defaultValuesPopulationService);
    this.folioRecordService = folioRecordService;
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.HOLDINGS;
  }

  @Override
  protected void updateRecord(QuickMarcRecord qmRecord) {
    log.debug("update:: Updating holdings record with id: {}", qmRecord.getExternalId());

    updateSrsRecord(qmRecord);
    var holdingId = qmRecord.getExternalId();
    var existingHoldings = folioRecordService.get(holdingId);

    var mappedHolding = getMappedRecord(qmRecord, existingHoldings);
    folioRecordService.update(holdingId, mappedHolding);
    log.debug("update:: Holdings record with id: {} has been updated successfully", qmRecord.getExternalId());
  }

  @Override
  protected void createRecord(QuickMarcRecord qmRecord) {
    log.debug("create:: Creating new holdings record");

    // Step 1: Map QuickMarcRecord to Holdings using ParsedContent
    var mappedHoldings = getMappedRecord(qmRecord);

    // Step 3: Create Holdings in storage (gets generated ID and HRID)
    var createdHoldings = folioRecordService.create(mappedHoldings);
    log.debug("create:: Holdings created with id: {}", createdHoldings.getId());

    // Step 4: Update QuickMarcRecord with generated IDs
    qmRecord.setExternalId(UUID.fromString(createdHoldings.getId()));
    qmRecord.setExternalHrid(createdHoldings.getHrid());

    // Step 5: Create SRS record with external IDs (add 001 field for Holdings)
    createSrsRecord(qmRecord, true);  // true = add 001 field with HRID

    // Step 6: Convert to QuickMarcView and return
    qmRecord.setFolioRecord(createdHoldings);
  }

  @Override
  protected ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord) {
    return new ExternalIdsHolder()
      .withHoldingsId(qmRecord.getExternalId().toString())
      .withHoldingsHrid(qmRecord.getExternalHrid());
  }
}
