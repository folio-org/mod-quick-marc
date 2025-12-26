package org.folio.qm.service.mapping;

import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.convertion.merger.FolioRecordMerger;
import org.folio.qm.domain.model.FolioRecord;
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
    return toFolioRecord(getMappedRecord(qmRecord), null);
  }

  @Override
  public F mapUpdatedRecord(QuickMarcRecord qmRecord, F folioRecord) {
    return toFolioRecord(getMappedRecord(qmRecord), folioRecord);
  }

  protected E getMappedRecord(QuickMarcRecord qmRecord) {
    var mappingRecordType = qmRecord.getMappingRecordType();
    try {
      var mappingMetadata = mappingMetadataProvider.getMappingData(mappingRecordType);
      if (Objects.isNull(mappingMetadata)) {
        throw new MappingMetadataException(
          String.format("mapping metadata not found for %s record with parsedRecordId: %s",
            mappingRecordType, qmRecord.getParsedRecordId()));
      }
      var recordMapper = getRecordMapper();
      return recordMapper.mapRecord(
        qmRecord.getParsedContent(),
        mappingMetadata.mappingParameters(),
        mappingMetadata.mappingRules());
    } catch (MappingMetadataException e) {
      throw e;
    } catch (Exception e) {
      throw new MappingMetadataException(
        String.format("Error mapping %s record with parsedRecordId: %s", mappingRecordType,
          qmRecord.getParsedRecordId()), e);
    }
  }

  protected abstract RecordMapper<E> getRecordMapper();

  protected abstract F initFolioRecord();

  protected void postProcess(F folioRecord) {
    log.debug("Post processing of folio record: {}", folioRecord);
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
