package org.folio.qm.converter;

import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.service.impl.DefaultValuesPopulationService;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Converts QuickMarcEdit DTO to QuickMarcRecord domain object.
 * Prepares the context holder with marc4j Record, ParsedContent, and metadata for update operations.
 *
 * <p>Conversion flow:
 * <ol>
 *   <li>QuickMarcEdit → marc4j Record</li>
 *   <li>marc4j Record → ParsedContent (JsonNode)</li>
 *   <li>Store both in QuickMarcRecord for reuse</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class QuickMarcEditToRecordConverter implements Converter<QuickMarcEdit, QuickMarcRecord> {

  private final DefaultValuesPopulationService defaultValuesPopulationService;
  private final Converter<BaseMarcRecord, Record> marcConverter;
  private final MarcTypeMapper typeMapper;

  @Override
  public QuickMarcRecord convert(@NonNull QuickMarcEdit source) {
    // Step 1: Convert to marc4j Record
    defaultValuesPopulationService.populate(source);
    var marcRecord = marcConverter.convert(source);

    return QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .source(source)
      .marcFormat(source.getMarcFormat())
      .mappingRecordType(typeMapper.fromDto(source.getMarcFormat()))
      .srsRecordType(typeMapper.fromDtoToSrsType(source.getMarcFormat()))
      .sourceVersion(source.getSourceVersion())
      .suppressDiscovery(source.getSuppressDiscovery())
      .externalId(source.getExternalId())
      .externalHrid(source.getExternalHrid())
      .parsedRecordId(source.getParsedRecordId())
      .parsedRecordDtoId(source.getParsedRecordDtoId())
      .build();
  }
}
