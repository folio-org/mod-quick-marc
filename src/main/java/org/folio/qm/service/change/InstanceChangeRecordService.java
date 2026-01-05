package org.folio.qm.service.change;

import lombok.extern.log4j.Log4j2;
import org.folio.ExternalIdsHolder;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.links.LinksService;
import org.folio.qm.service.mapping.MarcMappingService;
import org.folio.qm.service.population.DefaultValuesPopulationService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.ValidationService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class InstanceChangeRecordService extends AbstractChangeRecordService<InstanceRecord> {

  private final LinksService linksService;

  protected InstanceChangeRecordService(ValidationService validationService,
                                        RecordConversionService conversionService,
                                        SourceRecordService sourceRecordService,
                                        MarcMappingService<InstanceRecord> mappingService,
                                        FolioRecordService<InstanceRecord> folioRecordService,
                                        LinksService linksService,
                                        DefaultValuesPopulationService defaultValuesPopulationService) {
    super(validationService, conversionService, sourceRecordService, mappingService, folioRecordService,
      defaultValuesPopulationService);
    this.linksService = linksService;
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.BIBLIOGRAPHIC;
  }

  @Override
  protected void postProcess(QuickMarcRecord qmRecord) {
    super.postProcess(qmRecord);
    log.debug("postProcess:: Updating links for instance with id: {}", qmRecord.getExternalId());
    linksService.updateRecordLinks(qmRecord);
    log.debug("postProcess:: Links updated successfully for instance with id: {}", qmRecord.getExternalId());
  }

  @Override
  protected ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord) {
    return new ExternalIdsHolder()
      .withInstanceId(qmRecord.getExternalId().toString())
      .withInstanceHrid(qmRecord.getExternalHrid());
  }

  @Override
  protected boolean adding001FieldRequired() {
    return true;
  }
}
