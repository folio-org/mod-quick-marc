package org.folio.qm.convertion.merger;

import org.folio.Authority;
import org.folio.qm.domain.model.AuthorityRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AuthorityRecordMerger extends FolioRecordMerger<AuthorityRecord, Authority> {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void merge(Authority source, @MappingTarget AuthorityRecord target);
}
