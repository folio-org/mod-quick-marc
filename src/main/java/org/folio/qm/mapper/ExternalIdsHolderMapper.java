package org.folio.qm.mapper;

import org.folio.ExternalIdsHolder;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExternalIdsHolderMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  ExternalIdsHolder toExternalIdsHolder(org.folio.qm.client.model.ExternalIdsHolder source);
}
