package org.folio.qm.service.mapping;

import static org.apache.commons.lang.StringUtils.isBlank;

import lombok.extern.log4j.Log4j2;
import org.folio.Holdings;
import org.folio.processing.mapping.defaultmapper.MarcToHoldingsMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.HoldingsRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.storage.folio.FolioRecordInstanceService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.util.MarcRecordModifier;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MarcMappingHoldingsService extends MarcMappingAbstractService<HoldingsRecord, Holdings> {

  private final FolioRecordInstanceService folioRecordInstanceService;

  public MarcMappingHoldingsService(MappingMetadataProvider mappingMetadataProvider,
                                    FolioRecordMerger<HoldingsRecord, Holdings> merger,
                                    FolioRecordInstanceService folioRecordInstanceService) {
    super(mappingMetadataProvider, merger);
    this.folioRecordInstanceService = folioRecordInstanceService;
  }

  @Override
  protected RecordMapper<Holdings> getRecordMapper() {
    return new MarcToHoldingsMapper();
  }

  @Override
  protected HoldingsRecord initFolioRecord() {
    return new HoldingsRecord();
  }

  @Override
  protected void mapRequiredFields(QuickMarcRecord qmRecord, @NotNull Holdings mappedRecord, boolean isNewRecord) {
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
