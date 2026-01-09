package org.folio.qm.service.mapping;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.folio.Holdings;
import org.folio.processing.mapping.defaultmapper.MarcToHoldingsMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.HoldingsRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.util.MarcRecordModifier;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MarcMappingHoldingsService extends MarcMappingAbstractService<HoldingsRecord, Holdings> {

  private final InstanceStorageClient instanceStorageClient;

  public MarcMappingHoldingsService(MappingMetadataProvider mappingMetadataProvider,
                                    FolioRecordMerger<HoldingsRecord, Holdings> merger,
                                    InstanceStorageClient instanceStorageClient) {
    super(mappingMetadataProvider, merger);
    this.instanceStorageClient = instanceStorageClient;
  }

  @Override
  protected RecordMapper<Holdings> getRecordMapper() {
    return new MarcToHoldingsMapper();
  }

  @Override
  protected HoldingsRecord initFolioRecord() {
    return new HoldingsRecord();
  }

  @Override
  public HoldingsRecord mapNewRecord(QuickMarcRecord qmRecord) {
    var holdingsRecord = super.mapNewRecord(qmRecord);
    holdingsRecord.setInstanceId(getInstanceId(qmRecord));
    return holdingsRecord;
  }

  private String getInstanceId(QuickMarcRecord qmRecord) {
    var instanceHrid = MarcRecordModifier.get004ControlFieldData(qmRecord.getMarcRecord());
    if (isBlank(instanceHrid)) {
      log.warn("setInstanceId:: 004 field is missing or empty for externalId: {}", qmRecord.getExternalId());
      throw new IllegalStateException("004 field is missing or empty in MARC holdings record");
    }
    return Optional.ofNullable(instanceStorageClient.getInstanceByHrid(instanceHrid).getInstanceId())
      .orElseThrow(() -> new IllegalStateException(
        String.format("Instance ID is missing or more than one instance found for HRID: %s", instanceHrid)));
  }
}
