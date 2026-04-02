package org.folio.qm.convertion.merger;

import org.folio.qm.domain.model.HoldingsFolioRecord;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HoldingsRecordMerger extends FolioRecordMerger<HoldingsFolioRecord, HoldingsRecord> {

  @Mapping(target = "statisticalCodeIds", ignore = true)
  @Mapping(target = "administrativeNotes", ignore = true)
  @Mapping(target = "additionalCallNumbers", ignore = true)
  @Mapping(target = "illPolicyId", ignore = true)
  @Mapping(target = "digitizationPolicy", ignore = true)
  @Mapping(target = "retentionPolicy", ignore = true)
  @Mapping(target = "acquisitionFormat", ignore = true)
  @Mapping(target = "acquisitionMethod", ignore = true)
  @Mapping(target = "receivingHistory", ignore = true)
  @Mapping(target = "temporaryLocationId", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  void merge(HoldingsRecord source, @MappingTarget HoldingsFolioRecord target);

  @BeforeMapping
  default void beforeMerge(HoldingsRecord source, @MappingTarget HoldingsFolioRecord target) {
    source.setId(target.getId());
    source.setVersion(target.getVersion());
  }
}
