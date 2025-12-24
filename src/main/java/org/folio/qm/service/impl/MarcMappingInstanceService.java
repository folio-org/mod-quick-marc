package org.folio.qm.service.impl;

import org.folio.Instance;
import org.folio.processing.mapping.defaultmapper.MarcToInstanceMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.mapper.InstanceRecordMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class MarcMappingInstanceService extends MarcMappingAbstractService<InstanceRecord, Instance> {

  private final InstanceRecordMapper mapper;

  public MarcMappingInstanceService(MappingMetadataProvider mappingMetadataProvider, InstanceRecordMapper mapper) {
    super(mappingMetadataProvider);
    this.mapper = mapper;
  }

  @Override
  protected RecordMapper<Instance> getRecordMapper() {
    return new MarcToInstanceMapper();
  }

  @Override
  protected InstanceRecord toFolioRecord(@NonNull Instance mappedRecord, @Nullable InstanceRecord folioRecord) {
    if (folioRecord == null) {
      folioRecord = new InstanceRecord();
    }
    var id = folioRecord.getId();
    mapper.merge(mappedRecord, folioRecord);
    folioRecord.setId(id);
    return folioRecord;
  }
}
