package org.folio.qm.convertion.merger;

import org.folio.Instance;
import org.folio.qm.domain.model.InstanceFolioRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface InstanceRecordMerger extends FolioRecordMerger<InstanceFolioRecord, Instance> {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void merge(Instance source, @MappingTarget InstanceFolioRecord target);
}
