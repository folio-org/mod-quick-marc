package org.folio.qm.converter;

import org.folio.Instance;
import org.folio.qm.domain.model.InstanceRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface InstanceRecordMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void merge(Instance source, @MappingTarget InstanceRecord target);
}
