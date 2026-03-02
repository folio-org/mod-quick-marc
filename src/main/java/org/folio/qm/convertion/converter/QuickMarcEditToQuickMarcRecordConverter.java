package org.folio.qm.convertion.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.folio.qm.domain.model.MappingRecordType;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts QuickMarcEdit DTO to QuickMarcRecord domain object.
 * Prepares the context holder with marc4j Record, ParsedContent, and metadata for update operations.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class QuickMarcEditToQuickMarcRecordConverter implements Converter<QuickMarcEdit, QuickMarcRecord> {

  private final Converter<BaseQuickMarcRecord, Record> marcConverter;
  private final Converter<MarcFormat, MappingRecordType> toMappingRecordTypeConverter;
  private final Converter<MarcFormat, RecordType> toRecordTypeConverter;

  @Override
  public QuickMarcRecord convert(QuickMarcEdit source) {
    log.info("Converting QuickMarcEdit to QuickMarcRecord for getParsedRecordDtoId: {}", source.getParsedRecordDtoId());
    var marcRecord = marcConverter.convert(source);
    log.info("After converting QuickMarcEdit to QuickMarcRecord for getParsedRecordDtoId: {}",
      source.getParsedRecordDtoId());
    return QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .source(source)
      .marcFormat(source.getMarcFormat())
      .mappingRecordType(toMappingRecordTypeConverter.convert(source.getMarcFormat()))
      .sourceRecordType(toRecordTypeConverter.convert(source.getMarcFormat()))
      .sourceVersion(source.getSourceVersion())
      .suppressDiscovery(source.getSuppressDiscovery())
      .externalId(source.getExternalId())
      .externalHrid(source.getExternalHrid())
      .parsedRecordId(source.getParsedRecordId())
      .parsedRecordDtoId(source.getParsedRecordDtoId())
      .build();
  }
}
