package org.folio.qm.service.mapping;

import static org.apache.commons.lang.StringUtils.isBlank;

import lombok.extern.log4j.Log4j2;
import org.folio.processing.mapping.defaultmapper.MarcToHoldingsMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.HoldingsFolioRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.storage.folio.FolioRecordInstanceService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.util.MarcRecordModifier;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MarcMappingHoldingsService extends MarcMappingAbstractService<HoldingsFolioRecord, HoldingsRecord> {

  private final FolioRecordInstanceService folioRecordInstanceService;

  public MarcMappingHoldingsService(MappingMetadataProvider mappingMetadataProvider,
                                    FolioRecordMerger<HoldingsFolioRecord, HoldingsRecord> merger,
                                    FolioRecordInstanceService folioRecordInstanceService) {
    super(mappingMetadataProvider, merger);
    this.folioRecordInstanceService = folioRecordInstanceService;
  }

  @Override
  protected RecordMapper<HoldingsRecord> getRecordMapper() {
    return new MarcToHoldingsMapper();
  }

  @Override
  protected HoldingsFolioRecord initFolioRecord() {
    return new HoldingsFolioRecord();
  }

  @Override
  protected void mapRequiredFields(QuickMarcRecord qmRecord,
                                   @NonNull HoldingsRecord mappedRecord,
                                   boolean isNewRecord) {
    if (isNewRecord) {
      var instanceHrid = MarcRecordModifier.get004ControlFieldData(qmRecord.getMarcRecord());
      if (isBlank(instanceHrid)) {
        log.warn("setInstanceId:: 004 field is missing or empty for externalId: {}", qmRecord.getExternalId());
        throw new IllegalStateException("004 field is missing or empty in MARC holdings record");
      }
      var instanceId = folioRecordInstanceService.getInstanceIdByHrid(instanceHrid);
      mappedRecord.setInstanceId(instanceId);
    }
  }
}
