package org.folio.qm.convertion.converter;

import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcCreate;
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

  private final Converter<BaseMarcRecord, Record> marcConverter;
  private final MarcFormatToMappingRecordTypeConverter toMappingRecordTypeConverter;
  private final MarcFormatToRecordTypeConverter toSrsRecordTypeConverter;

  @Override
  public QuickMarcRecord convert(QuickMarcCreate source) {
    var marcRecord = marcConverter.convert(source);

    return QuickMarcRecord.builder()
      .source(source)
      .marcRecord(marcRecord)
      .marcFormat(source.getMarcFormat())
      .srsRecordType(toSrsRecordTypeConverter.convert(source.getMarcFormat()))
      .mappingRecordType(toMappingRecordTypeConverter.convert(source.getMarcFormat()))
      .suppressDiscovery(source.getSuppressDiscovery())
      .build();
  }
}
