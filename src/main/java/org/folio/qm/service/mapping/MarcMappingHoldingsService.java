package org.folio.qm.service.mapping;

import org.folio.Holdings;
import org.folio.processing.mapping.defaultmapper.MarcToHoldingsMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.HoldingsRecord;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.springframework.stereotype.Service;

@Service
public class MarcMappingHoldingsService extends MarcMappingAbstractService<HoldingsRecord, Holdings> {

  public MarcMappingHoldingsService(MappingMetadataProvider mappingMetadataProvider,
                                    FolioRecordMerger<HoldingsRecord, Holdings> merger) {
    super(mappingMetadataProvider, merger);
  }

  @Override
  protected RecordMapper<Holdings> getRecordMapper() {
    return new MarcToHoldingsMapper();
  }

  @Override
  protected HoldingsRecord initFolioRecord() {
    return new HoldingsRecord();
  }
}
