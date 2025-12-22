package org.folio.qm.service.impl;

import static org.folio.qm.client.model.RecordTypeEnum.AUTHORITY;

import lombok.extern.log4j.Log4j2;
import org.folio.ActionProfile;
import org.folio.Authority;
import org.folio.ExternalIdsHolder;
import org.folio.qm.client.AuthorityStorageClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.converter.MarcQmConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.mapper.AuthorityRecordMapper;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.service.RecordService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuthorityRecordService extends RecordService<Authority> {

  private static final String AUTHORITY_EXTENDED = "AUTHORITY_EXTENDED";

  private final AuthorityStorageClient authorityStorageClient;
  private final AuthorityRecordMapper mapper;

  protected AuthorityRecordService(MappingMetadataProvider mappingMetadataProvider,
                                   SourceStorageClient sourceStorageClient,
                                   MarcQmConverter<QuickMarcEdit> marcQmConverter,
                                   MarcTypeMapper typeMapper,
                                   AuthorityStorageClient authorityStorageClient,
                                   AuthorityRecordMapper mapper) {
    super(mappingMetadataProvider, sourceStorageClient, marcQmConverter, typeMapper);
    this.authorityStorageClient = authorityStorageClient;
    this.mapper = mapper;
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.AUTHORITY;
  }

  @Override
  public void update(QuickMarcEdit quickMarc) {
    updateSrsRecord(quickMarc);
    var mappedAuthority = getMappedRecord(quickMarc);
    var authorityId = quickMarc.getExternalId().toString();
    var existingAuthority = authorityStorageClient.getAuthorityById(authorityId);
    if (existingAuthority == null) {
      throw new NotFoundException(String.format("Authority record with id: %s not found", authorityId));
    }
    var updatedAuthority = prepareForUpdate(existingAuthority, mappedAuthority);
    authorityStorageClient.updateAuthority(authorityId, updatedAuthority);
    log.debug("Authority record with id: {} has been updated successfully", authorityId);
  }

  @Override
  public ExternalIdsHolder getExternalIdsHolder(QuickMarcEdit quickMarc) {
    return new ExternalIdsHolder()
      .withAuthorityId(quickMarc.getExternalId().toString())
      .withAuthorityHrid(quickMarc.getExternalHrid());
  }

  @Override
  public MappingRecordTypeEnum getMapperRecordType() {
    return MappingRecordTypeEnum.MARC_AUTHORITY;
  }

  @Override
  public String getMapperName() {
    return Boolean.parseBoolean(System.getenv().getOrDefault(AUTHORITY_EXTENDED, "false"))
      ? ActionProfile.FolioRecord.MARC_AUTHORITY_EXTENDED.value()
      : AUTHORITY.getValue();
  }

  private Authority prepareForUpdate(Authority existingRecord, Authority mappedRecord) {
    mappedRecord.setId(existingRecord.getId());
    mappedRecord.setVersion(existingRecord.getVersion());
    mappedRecord.setSource(Authority.Source.MARC);
    mapper.merge(mappedRecord, existingRecord);
    return existingRecord;
  }
}
