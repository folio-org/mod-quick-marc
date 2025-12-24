package org.folio.qm.service.impl;

import org.folio.Authority;
import org.folio.processing.mapping.defaultmapper.MarcToAuthorityMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.qm.mapper.AuthorityRecordMapper;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class MarcMappingAuthorityService extends MarcMappingAbstractService<AuthorityRecord, Authority> {

  private final AuthorityRecordMapper mapper;

  public MarcMappingAuthorityService(MappingMetadataProvider mappingMetadataProvider, AuthorityRecordMapper mapper) {
    super(mappingMetadataProvider);
    this.mapper = mapper;
  }

  @Override
  protected RecordMapper<Authority> getRecordMapper() {
    return new MarcToAuthorityMapper();
  }

  @Override
  protected AuthorityRecord toFolioRecord(@NonNull Authority mappedRecord, @Nullable AuthorityRecord folioRecord) {
    if (folioRecord == null) {
      folioRecord = new AuthorityRecord();
    }
    mapper.merge(mappedRecord, folioRecord);
    folioRecord.setSource(Authority.Source.MARC);
    return folioRecord;
  }
}
