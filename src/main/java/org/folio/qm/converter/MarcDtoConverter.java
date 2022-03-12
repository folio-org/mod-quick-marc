package org.folio.qm.converter;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.folio.qm.domain.dto.UpdateInfo.RecordStateEnum.fromValue;
import static org.folio.qm.util.MarcUtils.masqueradeBlanks;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.field.VariableFieldConverter;
import org.folio.qm.domain.dto.ExternalIdsHolder;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.UpdateInfo;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapper;

@Component
@RequiredArgsConstructor
public class MarcDtoConverter implements Converter<ParsedRecordDto, QuickMarc> {

  private static final Map<MarcFormat, Function<ExternalIdsHolder, UUID>> externalIdExtractors = Map.of(
    MarcFormat.BIBLIOGRAPHIC, ExternalIdsHolder::getInstanceId,
    MarcFormat.HOLDINGS, ExternalIdsHolder::getHoldingsId,
    MarcFormat.AUTHORITY, ExternalIdsHolder::getAuthorityId
  );

  private static final Map<MarcFormat, Function<ExternalIdsHolder, String>> externalHridExtractors = Map.of(
    MarcFormat.BIBLIOGRAPHIC, ExternalIdsHolder::getInstanceHrid,
    MarcFormat.HOLDINGS, ExternalIdsHolder::getHoldingsHrid,
    MarcFormat.AUTHORITY, ExternalIdsHolder::getAuthorityHrid
  );

  private final ObjectMapper objectMapper;
  private final MarcTypeMapper typeMapper;
  private final List<VariableFieldConverter<DataField>> dataFieldConverters;
  private final List<VariableFieldConverter<ControlField>> controlFieldConverters;

  @Override
  public QuickMarc convert(@NonNull ParsedRecordDto source) {
    ParsedRecord parsedRecord = source.getParsedRecord();
    Record marcRecord = extractMarcRecord(parsedRecord);

    MarcFormat marcFormat = typeMapper.fromDto(source.getRecordType());
    List<FieldItem> fields = convertFields(marcRecord, marcFormat);
    String leader = convertLeader(marcRecord);

    return new QuickMarc().leader(leader)
      .fields(fields)
      .marcFormat(marcFormat)
      .parsedRecordDtoId(source.getId())
      .parsedRecordId(parsedRecord.getId())
      .externalId(externalIdExtractors.get(marcFormat).apply(source.getExternalIdsHolder()))
      .externalHrid(externalHridExtractors.get(marcFormat).apply(source.getExternalIdsHolder()))
      .suppressDiscovery(source.getAdditionalInfo().getSuppressDiscovery())
      .updateInfo(new UpdateInfo()
        .recordState(fromValue(source.getRecordState().getValue()))
        .updateDate(convertDate(source)));
  }

  private Record extractMarcRecord(ParsedRecord parsedRecord) {
    try (var input = IOUtils.toInputStream(objectMapper.writeValueAsString(parsedRecord), UTF_8)) {
      return new MarcJsonReader(input).next();
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private List<FieldItem> convertFields(Record marcRecord, MarcFormat marcFormat) {
    var controlFields = marcRecord.getControlFields().stream()
      .map(cf -> controlFieldToQuickMarcField(cf, marcRecord.getLeader(), marcFormat));
    var dataFields = marcRecord.getDataFields().stream()
      .map(field -> dataFieldToQuickMarcField(field, marcRecord.getLeader(), marcFormat));
    return Stream.concat(controlFields, dataFields).collect(Collectors.toList());
  }

  private String convertLeader(Record marcRecord) {
    return masqueradeBlanks(marcRecord.getLeader().marshal());
  }

  private OffsetDateTime convertDate(ParsedRecordDto parsedRecordDto) {
    var updatedDate = parsedRecordDto.getMetadata().getUpdatedDate();
    return updatedDate != null ? OffsetDateTime.ofInstant(updatedDate.toInstant(), ZoneId.systemDefault()) : null;
  }

  private FieldItem controlFieldToQuickMarcField(ControlField field, Leader leader, MarcFormat marcFormat) {
    return controlFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"))
      .indicators(Collections.emptyList());
  }

  private FieldItem dataFieldToQuickMarcField(DataField field, Leader leader, MarcFormat marcFormat) {
    return dataFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"));
  }

}
