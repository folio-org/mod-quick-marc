package org.folio.qm.service.impl;

import org.folio.Holdings;
import org.folio.processing.mapping.defaultmapper.MarcToHoldingsMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.domain.HoldingsRecord;
import org.folio.qm.mapper.HoldingsRecordMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class MarcMappingHoldingsService extends MarcMappingAbstractService<HoldingsRecord, Holdings> {

  private final HoldingsRecordMapper mapper;

  public MarcMappingHoldingsService(MappingMetadataProvider mappingMetadataProvider, HoldingsRecordMapper mapper) {
    super(mappingMetadataProvider);
    this.mapper = mapper;
  }

  @Override
  protected RecordMapper<Holdings> getRecordMapper() {
    return new MarcToHoldingsMapper();
  }

  @Override
  protected HoldingsRecord toFolioRecord(@NonNull Holdings mappedRecord, @Nullable HoldingsRecord folioRecord) {
    if (folioRecord == null) {
      folioRecord = new HoldingsRecord();
    }
    mapper.merge(mappedRecord, folioRecord);
    return folioRecord;
  }
}
