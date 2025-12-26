package org.folio.qm.service.change;

import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.ExternalIdsHolder;
import org.folio.qm.converter.QuickMarcRecordConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.links.LinksService;
import org.folio.qm.service.mapping.MarcMappingService;
import org.folio.qm.service.storage.folio.FolioRecordService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.validation.ValidationService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class InstanceChangeRecordService extends AbstractChangeRecordService<InstanceRecord> {

  private final FolioRecordService<InstanceRecord> folioRecordService;
  private final LinksService linksService;

  protected InstanceChangeRecordService(ValidationService validationService,
                                        Converter<QuickMarcCreate, QuickMarcRecord> quickMarcCreateQuickMarcRecordConverter,
                                        Converter<QuickMarcEdit, QuickMarcRecord> quickMarcEditQuickMarcRecordConverter,
                                        QuickMarcRecordConverter quickMarcRecordConverter,
                                        SourceRecordService sourceRecordService,
                                        MarcMappingService<InstanceRecord> mappingService,
                                        FolioRecordService<InstanceRecord> folioRecordService,
                                        LinksService linksService) {
    super(validationService, quickMarcCreateQuickMarcRecordConverter, quickMarcEditQuickMarcRecordConverter, quickMarcRecordConverter, sourceRecordService, mappingService);
    this.folioRecordService = folioRecordService;
    this.linksService = linksService;
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.BIBLIOGRAPHIC;
  }

  @Override
  public void update(QuickMarcRecord qmRecord) {
    log.debug("update:: Updating instance record with id: {}", qmRecord.getExternalId());

    updateSrsRecord(qmRecord);
    var instanceId = qmRecord.getExternalId();
    var existingInstance = folioRecordService.get(instanceId);
    var mappedInstance = getMappedRecord(qmRecord, existingInstance);
    folioRecordService.update(instanceId, mappedInstance);
    linksService.updateRecordLinks(qmRecord);
  }

  @Override
  public ExternalIdsHolder getExternalIdsHolder(QuickMarcRecord qmRecord) {
    return new ExternalIdsHolder()
      .withInstanceId(qmRecord.getExternalId().toString())
      .withInstanceHrid(qmRecord.getExternalHrid());
  }

  @Override
  public void create(QuickMarcRecord qmRecord) {
    log.debug("create:: Creating new instance record");

    // Step 1: Map QuickMarcRecord to org.folio.Instance using ParsedContent
    var mappedInstance = getMappedRecord(qmRecord);

    // Step 2: Convert to client model and create in storage (gets generated ID and HRID)
    var createdInstance = folioRecordService.create(mappedInstance);
    log.debug("create:: Instance created with id: {}", createdInstance.getId());

    // Step 4: Update QuickMarcRecord with generated IDs
    qmRecord.setExternalId(UUID.fromString(createdInstance.getId()));
    qmRecord.setExternalHrid(createdInstance.getHrid());

    // Step 5: Create SRS record with external IDs (add 001 field for Instance)
    createSrsRecord(qmRecord, true);  // true = add 001 field with HRID

    // Step 6: Convert to QuickMarcView and return
    qmRecord.setFolioRecord(createdInstance);
  }
}
