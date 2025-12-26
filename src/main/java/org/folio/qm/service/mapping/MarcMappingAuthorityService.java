package org.folio.qm.service.mapping;

import org.folio.Authority;
import org.folio.processing.mapping.defaultmapper.MarcToAuthorityMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.springframework.stereotype.Service;

@Service
public class MarcMappingAuthorityService extends MarcMappingAbstractService<AuthorityRecord, Authority> {

  public MarcMappingAuthorityService(MappingMetadataProvider mappingMetadataProvider,
                                     FolioRecordMerger<AuthorityRecord, Authority> merger) {
    super(mappingMetadataProvider, merger);
  }

  @Override
  protected RecordMapper<Authority> getRecordMapper() {
    return new MarcToAuthorityMapper();
  }

  @Override
  protected AuthorityRecord initFolioRecord() {
    return new AuthorityRecord();
  }

  @Override
  protected void postProcess(AuthorityRecord folioRecord) {
    folioRecord.setSource(Authority.Source.MARC);
  }
}
