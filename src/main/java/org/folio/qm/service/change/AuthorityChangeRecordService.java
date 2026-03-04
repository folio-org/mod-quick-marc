package org.folio.qm.service.change;

import lombok.extern.log4j.Log4j2;
import org.folio.ExternalIdsHolder;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.mapping.MarcMappingService;
import org.folio.qm.service.population.DefaultValuesPopulationService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.ValidationService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuthorityChangeRecordService extends AbstractChangeRecordService<AuthorityRecord> {

  protected AuthorityChangeRecordService(ValidationService validationService,
                                         RecordConversionService conversionService,
                                         SourceRecordService sourceRecordService,
                                         MarcMappingService<AuthorityRecord> mappingService,
                                         FolioRecordService<AuthorityRecord> folioRecordService,
                                         DefaultValuesPopulationService defaultValuesPopulationService) {
    super(validationService, conversionService, sourceRecordService, mappingService, folioRecordService,
      defaultValuesPopulationService);
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.AUTHORITY;
  }

  @Override
  protected ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord) {
    return new ExternalIdsHolder()
      .withAuthorityId(qmRecord.getExternalId().toString())
      .withAuthorityHrid(qmRecord.getExternalHrid());
  }

  @Override
  protected boolean adding001FieldRequired() {
    return false;
  }
}
