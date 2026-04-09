package org.folio.qm.service.change;

import lombok.extern.log4j.Log4j2;
import org.folio.ExternalIdsHolder;
import org.folio.processing.util.MarcRecordNormalizer;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.InstanceFolioRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.links.LinksService;
import org.folio.qm.service.mapping.MarcMappingService;
import org.folio.qm.service.population.DefaultValuesPopulationService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.ValidationService;
import org.folio.qm.util.MarcRecordModifier;
import org.folio.qm.util.MarcUtils;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class InstanceChangeRecordService extends AbstractChangeRecordService<InstanceFolioRecord> {

  private final LinksService linksService;

  protected InstanceChangeRecordService(ValidationService validationService,
                                        RecordConversionService conversionService,
                                        SourceRecordService sourceRecordService,
                                        MarcMappingService<InstanceFolioRecord> mappingService,
                                        FolioRecordService<InstanceFolioRecord> folioRecordService,
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
  protected void updateNonRequiredFields(QuickMarcRecord qmRecord) {
    log.debug("updateNonRequiredFields:: removing 003 fields from the instance marc record if exists");
    var marcRecord = qmRecord.getMarcRecord();
    MarcRecordModifier.remove003Field(marcRecord);
    log.debug("updateNonRequiredFields:: normalizing 035 fields in the instance marc record if exists");
    MarcRecordNormalizer.normalize035Field(marcRecord);
    qmRecord.buildParsedContent();
  }

  @Override
  protected void postProcess(QuickMarcRecord qmRecord) {
    super.postProcess(qmRecord);
    log.debug("postProcess:: Updating links for instance with id: {}", qmRecord.getExternalId());
    linksService.updateRecordLinks(qmRecord);
    log.debug("postProcess:: Links updated successfully for instance with id: {}", qmRecord.getExternalId());
  }

  @Override
  protected void postProcessMappedRecord(QuickMarcRecord qmRecord, InstanceFolioRecord mappedRecord) {
    if (MarcUtils.isLeaderStatusDeleted(qmRecord.getSource().getLeader())) {
      log.debug("postProcessMappedRecord:: LDR/05 is 'd', setting staffSuppress and discoverySuppress to true");
      mappedRecord.setStaffSuppress(true);
      mappedRecord.setDiscoverySuppress(true);
    }
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
