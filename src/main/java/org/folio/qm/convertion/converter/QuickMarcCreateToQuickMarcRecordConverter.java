package org.folio.qm.convertion.converter;

import lombok.RequiredArgsConstructor;
import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts QuickMarcCreate DTO to QuickMarcRecord domain object.
 * Used for CREATE operations where external IDs don't exist yet.
 */
@Component
@RequiredArgsConstructor
public class QuickMarcCreateToQuickMarcRecordConverter implements Converter<QuickMarcCreate, QuickMarcRecord> {

  private final Converter<BaseQuickMarcRecord, Record> marcConverter;
  private final Converter<MarcFormat, MappingRecordType> toMappingRecordTypeConverter;
  private final Converter<MarcFormat, RecordType> toRecordTypeConverter;

  @Override
  public QuickMarcRecord convert(QuickMarcCreate source) {
    var marcRecord = marcConverter.convert(source);

    return QuickMarcRecord.builder()
      .source(source)
      .marcRecord(marcRecord)
      .marcFormat(source.getMarcFormat())
      .sourceRecordType(toRecordTypeConverter.convert(source.getMarcFormat()))
      .mappingRecordType(toMappingRecordTypeConverter.convert(source.getMarcFormat()))
      .suppressDiscovery(source.getSuppressDiscovery())
      .build();
  }
}
