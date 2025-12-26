package org.folio.qm.converter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.folio.qm.util.MarcUtils.masqueradeBlanks;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.folio.ExternalIdsHolder;
import org.folio.ParsedRecord;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.dto.RecordState;
import org.folio.qm.domain.dto.UpdateInfo;
import org.folio.qm.exception.ConverterException;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SourceRecordConverter implements Converter<org.folio.Record, QuickMarcView> {

  private static final Map<MarcFormat, Function<ExternalIdsHolder, String>> EXTERNAL_ID_EXTRACTORS = Map.of(
    MarcFormat.BIBLIOGRAPHIC, ExternalIdsHolder::getInstanceId,
    MarcFormat.HOLDINGS, ExternalIdsHolder::getHoldingsId,
    MarcFormat.AUTHORITY, ExternalIdsHolder::getAuthorityId
  );

  private static final Map<MarcFormat, Function<ExternalIdsHolder, String>> EXTERNAL_HRID_EXTRACTORS = Map.of(
    MarcFormat.BIBLIOGRAPHIC, ExternalIdsHolder::getInstanceHrid,
    MarcFormat.HOLDINGS, ExternalIdsHolder::getHoldingsHrid,
    MarcFormat.AUTHORITY, ExternalIdsHolder::getAuthorityHrid
  );

  private final ObjectMapper objectMapper;
  private final MarcTypeMapper typeMapper;
  private final MarcFieldsConverter fieldsConverter;

  @Override
  public QuickMarcView convert(@NonNull org.folio.Record source) {
    var parsedRecord = source.getParsedRecord();
    var marcRecord = extractMarcRecord(parsedRecord);

    var format = typeMapper.fromDto(source.getRecordType());
    var leader = convertLeader(marcRecord);
    var fields = fieldsConverter.convertDtoFields(marcRecord.getVariableFields(), marcRecord.getLeader(), format);
    var reorderedFields = fieldsConverter.reorderFieldsBasedOnParsedRecordOrder(fields, parsedRecord);

    return new QuickMarcView()
      .leader(leader)
      .fields(reorderedFields)
      .marcFormat(format)
      .parsedRecordId(UUID.fromString(parsedRecord.getId()))
      .parsedRecordDtoId(UUID.fromString(source.getId()))
      .sourceVersion(source.getGeneration())
      .externalId(UUID.fromString(EXTERNAL_ID_EXTRACTORS.get(format).apply(source.getExternalIdsHolder())))
      .externalHrid(EXTERNAL_HRID_EXTRACTORS.get(format).apply(source.getExternalIdsHolder()))
      .suppressDiscovery(source.getAdditionalInfo().getSuppressDiscovery())
      .updateInfo(new UpdateInfo()
        .recordState(RecordState.ACTUAL)
        .updateDate(getUpdatedDate(source)));
  }

  private OffsetDateTime getUpdatedDate(org.folio.Record source) {
    var updatedDate = source.getMetadata().getUpdatedDate();
    return updatedDate != null ? updatedDate.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
  }

  private Record extractMarcRecord(ParsedRecord parsedRecord) {
    try (var input = IOUtils.toInputStream(objectMapper.writeValueAsString(parsedRecord), UTF_8)) {
      return new MarcJsonReader(input).next();
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private String convertLeader(Record marcRecord) {
    return masqueradeBlanks(marcRecord.getLeader().marshal());
  }
}
