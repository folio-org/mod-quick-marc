package org.folio.qm.convertion.merger;

import org.folio.Authority;
import org.folio.qm.domain.model.AuthorityFolioRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AuthorityRecordMerger extends FolioRecordMerger<AuthorityFolioRecord, Authority> {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  void merge(Authority source, @MappingTarget AuthorityFolioRecord target);

  @BeforeMapping
  default void beforeMerge(Authority source, @MappingTarget AuthorityFolioRecord target) {
    source.setVersion(target.getVersion());
  }
}
