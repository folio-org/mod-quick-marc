package org.folio.qm.service.impl;

import java.util.Objects;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.qm.domain.FolioRecord;
import org.folio.qm.domain.QuickMarcRecord;
import org.folio.qm.exception.MappingMetadataException;
import org.folio.qm.service.MarcMappingService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class MarcMappingAbstractService<T extends FolioRecord, F> implements MarcMappingService<T> {

  private final MappingMetadataProvider mappingMetadataProvider;

  protected MarcMappingAbstractService(MappingMetadataProvider mappingMetadataProvider) {
    this.mappingMetadataProvider = mappingMetadataProvider;
  }

  @Override
  public T mapNewRecord(QuickMarcRecord qmRecord) {
    return toFolioRecord(getMappedRecord(qmRecord), null);
  }

  @Override
  public T mapUpdatedRecord(QuickMarcRecord qmRecord, T folioRecord) {
    return toFolioRecord(getMappedRecord(qmRecord), folioRecord);
  }

  protected F getMappedRecord(QuickMarcRecord qmRecord) {
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

  protected abstract RecordMapper<F> getRecordMapper();

  protected abstract T toFolioRecord(@NonNull F mappedRecord, @Nullable T folioRecord);
}
