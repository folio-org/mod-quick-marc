package org.folio.qm.converter;

import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.QuickMarcRecord;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.service.impl.DefaultValuesPopulationService;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Converts QuickMarcCreate DTO to QuickMarcRecord domain object.
 * Used for CREATE operations where external IDs don't exist yet.
 *
 * <p>Conversion flow:
 * <ol>
 *   <li>QuickMarcCreate → marc4j Record</li>
 *   <li>Store both in QuickMarcRecord (IDs are null, will be generated)</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class QuickMarcCreateToRecordConverter implements Converter<QuickMarcCreate, QuickMarcRecord> {

  private final DefaultValuesPopulationService defaultValuesPopulationService;
  private final Converter<BaseMarcRecord, Record> marcConverter;
  private final MarcTypeMapper typeMapper;

  @Override
  public QuickMarcRecord convert(@NonNull QuickMarcCreate source) {
    // Step 1: Convert to marc4j Record
    defaultValuesPopulationService.populate(source);
    var marcRecord = marcConverter.convert(source);

    // Step 3: Build QuickMarcRecord with both artifacts
    return QuickMarcRecord.builder()
      .source(source)
      .marcRecord(marcRecord)
      .marcFormat(source.getMarcFormat())
      .srsRecordType(typeMapper.fromDtoToSrsType(source.getMarcFormat()))
      .mappingRecordType(typeMapper.fromDto(source.getMarcFormat()))
      .suppressDiscovery(source.getSuppressDiscovery())
      .build();
  }
}
