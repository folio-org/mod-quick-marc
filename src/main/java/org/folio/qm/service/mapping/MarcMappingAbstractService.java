package org.folio.qm.service.mapping;

import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.FolioRecord;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.MappingMetadataException;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Log4j2
public abstract class MarcMappingAbstractService<F extends FolioRecord, E> implements MarcMappingService<F> {

  private final MappingMetadataProvider mappingMetadataProvider;
  private final FolioRecordMerger<F, E> merger;

  protected MarcMappingAbstractService(MappingMetadataProvider mappingMetadataProvider,
                                       FolioRecordMerger<F, E> merger) {
    this.mappingMetadataProvider = mappingMetadataProvider;
    this.merger = merger;
  }

  @Override
  public F mapNewRecord(QuickMarcRecord qmRecord) {
    log.debug("mapNewRecord:: Mapping new record with parsedRecordId: {}", qmRecord.getParsedRecordId());
    var result = toFolioRecord(getMappedRecord(qmRecord), null);
    log.info("mapNewRecord:: New record mapped successfully");
    return result;
  }

  @Override
  public F mapUpdatedRecord(QuickMarcRecord qmRecord, F folioRecord) {
    log.debug("mapUpdatedRecord:: Mapping updated record with parsedRecordId: {}, folioRecordId: {}",
      qmRecord.getParsedRecordId(), folioRecord.getId());
    var result = toFolioRecord(getMappedRecord(qmRecord), folioRecord);
    log.info("mapUpdatedRecord:: Updated record mapped successfully");
    return result;
  }

  protected abstract RecordMapper<E> getRecordMapper();

  protected abstract F initFolioRecord();

  protected void postProcess(F folioRecord) {
    log.debug("Post processing of folio record: {}", folioRecord);
  }

  private E getMappedRecord(QuickMarcRecord qmRecord) {
    var mappingRecordType = qmRecord.getMappingRecordType();
    var recordId = qmRecord.getParsedRecordId();
    log.debug("getMappedRecord:: Mapping record with parsedRecordId: {}, type: {}", recordId, mappingRecordType);
    try {
      var mappingMetadata = retrieveMappingMetadata(mappingRecordType);
      var recordMapper = getRecordMapper();
      var mappedRecord = recordMapper.mapRecord(
        qmRecord.getParsedContent(),
        mappingMetadata.mappingParameters(),
        mappingMetadata.mappingRules());
      log.debug("getMappedRecord:: Record mapped successfully for parsedRecordId: {}", recordId);
      return mappedRecord;
    } catch (MappingMetadataException e) {
      throw e;
    } catch (Exception e) {
      log.error("getMappedRecord:: Error mapping {} record with parsedRecordId: {}", mappingRecordType, recordId, e);
      throw new MappingMetadataException(
        String.format("Error mapping %s record with parsedRecordId: %s", mappingRecordType, recordId), e);
    }
  }

  private MappingMetadataProvider.MappingData retrieveMappingMetadata(MappingRecordType mappingRecordType) {
    var mappingMetadata = mappingMetadataProvider.getMappingData(mappingRecordType);
    if (Objects.isNull(mappingMetadata)) {
      log.error("retrieveMappingMetadata:: Mapping metadata not found for {} record", mappingRecordType);
      throw new MappingMetadataException(String.format("mapping metadata not found for %s record", mappingRecordType));
    }
    return mappingMetadata;
  }

  private F toFolioRecord(@NonNull E mappedRecord, @Nullable F folioRecord) {
    if (folioRecord == null) {
      folioRecord = initFolioRecord();
    }
    var id = folioRecord.getId();
    merger.merge(mappedRecord, folioRecord);
    folioRecord.setId(id);
    postProcess(folioRecord);
    return folioRecord;
  }
}
