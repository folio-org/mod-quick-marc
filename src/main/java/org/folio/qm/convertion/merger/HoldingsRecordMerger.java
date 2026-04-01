package org.folio.qm.convertion.merger;

import org.folio.qm.domain.model.HoldingsFolioRecord;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HoldingsRecordMerger extends FolioRecordMerger<HoldingsFolioRecord, HoldingsRecord> {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void merge(HoldingsRecord source, @MappingTarget HoldingsFolioRecord target);
}
