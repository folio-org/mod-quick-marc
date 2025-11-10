package org.folio.qm.mapper;

import org.folio.Holdings;
import org.folio.qm.domain.entity.HoldingsRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HoldingsRecordMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void merge(Holdings source, @MappingTarget HoldingsRecord target);
}
