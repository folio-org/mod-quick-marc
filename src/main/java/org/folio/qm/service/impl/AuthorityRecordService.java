package org.folio.qm.service.impl;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.ExternalIdsHolder;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.MarcMappingService;
import org.folio.qm.service.RecordService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuthorityRecordService extends RecordService<AuthorityRecord> {

  private final FolioRecordService<AuthorityRecord> folioRecordService;

  protected AuthorityRecordService(SourceRecordService sourceRecordService,
                                   MarcMappingService<AuthorityRecord> mappingService,
                                   FolioRecordService<AuthorityRecord> folioRecordService) {
    super(sourceRecordService, mappingService);
    this.folioRecordService = folioRecordService;
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.AUTHORITY;
  }

  @Override
  public void update(QuickMarcRecord qmRecord) {
    log.debug("update:: Updating authority record");

    updateSrsRecord(qmRecord);
    var authorityId = qmRecord.getExternalId();
    var existingAuthority = folioRecordService.get(authorityId);
    var mappedAuthority = getMappedRecord(qmRecord, existingAuthority);
    folioRecordService.update(authorityId, mappedAuthority);
    log.debug("update:: Authority record with id: {} has been updated successfully", authorityId);
  }

  @Override
  public ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord) {
    return new ExternalIdsHolder()
      .withAuthorityId(qmRecord.getExternalId().toString())
      .withAuthorityHrid(qmRecord.getExternalHrid());
  }

  @Override
  public void create(QuickMarcRecord qmRecord) {
    log.debug("create:: Creating new authority record");

    // Step 1: Map QuickMarcRecord to Authority
    var mappedAuthority = getMappedRecord(qmRecord);

    // Step 2: Create Authority in storage (gets generated ID and natural ID)
    var createdAuthority = folioRecordService.create(mappedAuthority);
    log.debug("create:: Authority created with id: {}", createdAuthority.getId());

    // Step 3: Update QuickMarcRecord with generated IDs
    qmRecord.setExternalId(UUID.fromString(createdAuthority.getId()));
    qmRecord.setExternalHrid(createdAuthority.getNaturalId());

    // Step 4: Create SRS record with external IDs (no 001 field for Authority)
    createSrsRecord(qmRecord, false);  // false = don't add 001 field

    // Step 5: Convert to QuickMarcView and return
    qmRecord.setFolioRecord(createdAuthority);
  }
}
