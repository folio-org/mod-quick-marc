package org.folio.qm.service.change;

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
import org.folio.qm.util.MarcRecordModifier;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class HoldingsChangeRecordService extends AbstractChangeRecordService<HoldingsRecord> {

  public HoldingsChangeRecordService(ValidationService validationService,
                                     RecordConversionService conversionService,
                                     SourceRecordService sourceRecordService,
                                     MarcMappingService<HoldingsRecord> mappingService,
                                     FolioRecordService<HoldingsRecord> folioRecordService,
                                     DefaultValuesPopulationService defaultValuesPopulationService) {
    super(validationService, conversionService, sourceRecordService, mappingService, folioRecordService,
      defaultValuesPopulationService);
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.HOLDINGS;
  }

  @Override
  public void updateNonRequiredFields(QuickMarcRecord qmRecord) {
    log.debug("updateNonRequiredFields:: removing 003 fields from holdings record if exists");
    var marcRecord = qmRecord.getMarcRecord();
    MarcRecordModifier.remove003Field(marcRecord);
    qmRecord.buildParsedContent();
  }

  @Override
  protected ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord) {
    return new ExternalIdsHolder()
      .withHoldingsId(qmRecord.getExternalId().toString())
      .withHoldingsHrid(qmRecord.getExternalHrid());
  }

  @Override
  protected boolean adding001FieldRequired() {
    return true;
  }
}
