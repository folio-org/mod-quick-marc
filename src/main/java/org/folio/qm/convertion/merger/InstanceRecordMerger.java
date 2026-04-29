package org.folio.qm.convertion.merger;

import org.folio.Instance;
import org.folio.qm.domain.model.InstanceFolioRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface InstanceRecordMerger extends FolioRecordMerger<InstanceFolioRecord, Instance> {

  @Mapping(target = "staffSuppress", ignore = true)
  @Mapping(target = "discoverySuppress", ignore = true)
  @Mapping(target = "statisticalCodeIds", ignore = true)
  @Mapping(target = "administrativeNotes", ignore = true)
  @Mapping(target = "natureOfContentTermIds", ignore = true)
  @Mapping(target = "previouslyHeld", ignore = true)
  @Mapping(target = "statusId", ignore = true)
  @Mapping(target = "statusUpdatedDate", ignore = true)
  @Mapping(target = "catalogedDate", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  void merge(Instance source, @MappingTarget InstanceFolioRecord target);

  @BeforeMapping
  default void beforeMerge(Instance source, @MappingTarget InstanceFolioRecord target) {
    source.setId(target.getId());
    source.setVersion(target.getVersion() == null ? null : Math.toIntExact(target.getVersion()));
  }
}
