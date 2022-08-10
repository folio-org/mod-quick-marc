package org.folio.qm.converter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.folio.qm.util.MarcUtils.masqueradeBlanks;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.folio.qm.domain.dto.ExternalIdsHolder;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.UpdateInfo;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapper;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcDtoConverter implements Converter<ParsedRecordDto, QuickMarc> {

  private static final Map<MarcFormat, Function<ExternalIdsHolder, UUID>> EXTERNAL_ID_EXTRACTORS = Map.of(
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
  public QuickMarc convert(@NonNull ParsedRecordDto source) {
    var parsedRecord = source.getParsedRecord();
    var marcRecord = extractMarcRecord(parsedRecord);

    var format = typeMapper.fromDto(source.getRecordType());
    var leader = convertLeader(marcRecord);
    var fields = fieldsConverter.convertDtoFields(marcRecord.getVariableFields(), marcRecord.getLeader(), format);

    return new QuickMarc()
      .leader(leader)
      .fields(fields)
      .marcFormat(format)
      .parsedRecordId(parsedRecord.getId())
      .parsedRecordDtoId(source.getId())
      .externalId(EXTERNAL_ID_EXTRACTORS.get(format).apply(source.getExternalIdsHolder()))
      .externalHrid(EXTERNAL_HRID_EXTRACTORS.get(format).apply(source.getExternalIdsHolder()))
      .suppressDiscovery(source.getAdditionalInfo().getSuppressDiscovery())
      .updateInfo(new UpdateInfo()
        .recordState(source.getRecordState())
        .updateDate(source.getMetadata().getUpdatedDate()));
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
