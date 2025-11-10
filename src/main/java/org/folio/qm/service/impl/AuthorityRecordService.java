package org.folio.qm.service.impl;

import static org.folio.qm.client.model.RecordTypeEnum.AUTHORITY;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.ActionProfile;
import org.folio.Authority;
import org.folio.qm.client.AuthorityStorageClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.mapper.AuthorityRecordMapper;
import org.folio.qm.mapper.ExternalIdsHolderMapper;
import org.folio.qm.service.RecordService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@Log4j2
public class AuthorityRecordService extends RecordService<Authority> {

  private static final String AUTHORITY_EXTENDED = "AUTHORITY_EXTENDED";

  private final AuthorityStorageClient authorityStorageClient;
  private final AuthorityRecordMapper mapper;

  protected AuthorityRecordService(MappingMetadataProvider mappingMetadataProvider,
                                   SourceStorageClient sourceStorageClient,
                                   ExternalIdsHolderMapper externalIdsHolderMapper,
                                   AuthorityStorageClient authorityStorageClient,
                                   AuthorityRecordMapper mapper) {
    super(mappingMetadataProvider, sourceStorageClient, externalIdsHolderMapper);
    this.authorityStorageClient = authorityStorageClient;
    this.mapper = mapper;
  }

  @Override
  public RecordTypeEnum supportedType() {
    return RecordTypeEnum.AUTHORITY;
  }

  @Override
  public void update(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                     ParsedRecordDto parsedRecordDto) {
    try {
      var mappedAuthority = getMappedAuthority(parsedRecordDto);
      if (mappedAuthority == null) {
        handleError(parsedRecordDto.getId(), updateResult,
          String.format("getMappedRecord:: mapping metadata not found for Authority record with parsedRecordId: %s",
            parsedRecordId));
        return;
      }
      var authorityId = parsedRecordDto.getExternalIdsHolder().getAuthorityId().toString();
      var existingAuthority = authorityStorageClient.getAuthorityById(authorityId);
      if (existingAuthority == null) {
        handleError(parsedRecordId, updateResult, String.format("Authority record with id: %s not found", authorityId));
        return;
      }
      var updatedAuthority = prepareForUpdate(existingAuthority, mappedAuthority);
      authorityStorageClient.updateAuthority(authorityId, updatedAuthority);
      log.debug("Authority record with id: {} has been updated successfully", authorityId);
      updateSrsRecord(parsedRecordId, updateResult, parsedRecordDto);
    } catch (Exception e) {
      handleError(parsedRecordId, updateResult,
        String.format("Error updating authority record for parsedRecordId: %s, error: %s",
          parsedRecordId, e.getMessage()), e);
    }
  }

  private Authority getMappedAuthority(ParsedRecordDto parsedRecordDto) {
    var mapperName = Boolean.parseBoolean(System.getenv().getOrDefault(AUTHORITY_EXTENDED, "false"))
      ? ActionProfile.FolioRecord.MARC_AUTHORITY_EXTENDED.value()
      : AUTHORITY.getValue();

    return getMappedRecord(parsedRecordDto, MappingRecordTypeEnum.MARC_AUTHORITY.getValue(), mapperName);
  }

  private Authority prepareForUpdate(Authority existingRecord, Authority mappedRecord) {
    mappedRecord.setId(existingRecord.getId());
    mappedRecord.setVersion(existingRecord.getVersion());
    mappedRecord.setSource(Authority.Source.MARC);
    mapper.merge(mappedRecord, existingRecord);
    return existingRecord;
  }
}
