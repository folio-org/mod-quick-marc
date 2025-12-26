package org.folio.qm.converter;

import org.folio.Holdings;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HoldingsRecordMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void merge(Holdings source, @MappingTarget HoldingsRecord target);
}
