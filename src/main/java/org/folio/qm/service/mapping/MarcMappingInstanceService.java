package org.folio.qm.service.mapping;

import org.folio.Instance;
import org.folio.processing.mapping.defaultmapper.MarcToInstanceMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.springframework.stereotype.Service;

@Service
public class MarcMappingInstanceService extends MarcMappingAbstractService<InstanceRecord, Instance> {

  public MarcMappingInstanceService(MappingMetadataProvider mappingMetadataProvider,
                                    FolioRecordMerger<InstanceRecord, Instance> merger) {
    super(mappingMetadataProvider, merger);
  }

  @Override
  protected RecordMapper<Instance> getRecordMapper() {
    return new MarcToInstanceMapper();
  }

  @Override
  protected InstanceRecord initFolioRecord() {
    return new InstanceRecord();
  }
}
