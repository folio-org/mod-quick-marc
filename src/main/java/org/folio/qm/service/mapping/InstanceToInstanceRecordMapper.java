package org.folio.qm.service.mapping;

import org.folio.qm.domain.model.InstanceRecord;
import org.folio.rest.jaxrs.model.Instance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InstanceToInstanceRecordMapper {

  @Mapping(target = "precedingTitles", ignore = true)
  @Mapping(target = "succeedingTitles", ignore = true)
  @Mapping(target = "source", constant = "CONSORTIUM-MARC")
  InstanceRecord toInstanceRecord(Instance instance);
}
