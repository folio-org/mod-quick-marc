package org.folio.qm.convertion.merger;

import org.folio.Holdings;
import org.folio.qm.domain.model.HoldingsRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HoldingsRecordMerger extends FolioRecordMerger<HoldingsRecord, Holdings> {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void merge(Holdings source, @MappingTarget HoldingsRecord target);
}
