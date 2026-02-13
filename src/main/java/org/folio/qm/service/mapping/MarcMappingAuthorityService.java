package org.folio.qm.service.mapping;

import static org.folio.ActionProfile.FolioRecord.MARC_AUTHORITY;
import static org.folio.ActionProfile.FolioRecord.MARC_AUTHORITY_EXTENDED;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.ActionProfile;
import org.folio.Authority;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.qm.service.storage.config.AuthoritiesConfigService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.springframework.stereotype.Service;

@Service
public class MarcMappingAuthorityService extends MarcMappingAbstractService<AuthorityRecord, Authority> {

  private final AuthoritiesConfigService authoritiesConfigService;
  private final Map<ActionProfile.FolioRecord, RecordMapper<Authority>> recordMappers;

  public MarcMappingAuthorityService(MappingMetadataProvider mappingMetadataProvider,
                                     FolioRecordMerger<AuthorityRecord, Authority> merger,
                                     AuthoritiesConfigService authoritiesConfigService,
                                     List<RecordMapper<Authority>> recordMappers) {
    super(mappingMetadataProvider, merger);
    this.authoritiesConfigService = authoritiesConfigService;
    this.recordMappers = recordMappers.stream().collect(Collectors.toMap(
      recordMapper -> ActionProfile.FolioRecord.fromValue(recordMapper.getMapperFormat()),
      Function.identity()));
  }

  @Override
  protected RecordMapper<Authority> getRecordMapper() {
    var isExtendedMappingEnabled = authoritiesConfigService.isAuthorityExtendedMappingEnabled();
    return isExtendedMappingEnabled
           ? recordMappers.get(MARC_AUTHORITY_EXTENDED)
           : recordMappers.get(MARC_AUTHORITY);
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
