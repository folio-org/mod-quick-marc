package org.folio.qm.converter;

import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

  private final MarcTypeMapper typeMapper;
  private final List<VariableFieldConverter<DataField>> dataFieldConverters;
  private final List<VariableFieldConverter<ControlField>> controlFieldConverters;

  @Override
  public QuickMarc convert(@NonNull ParsedRecordDto source) {
    ParsedRecord parsedRecord = source.getParsedRecord();

    try (InputStream input = IOUtils.toInputStream(new ObjectMapper().writeValueAsString(parsedRecord), StandardCharsets.UTF_8)) {
      Record marcRecord = new MarcJsonReader(input).next();
      String leader = masqueradeBlanks(marcRecord.getLeader().marshal());

      MarcFormat marcFormat = typeMapper.fromDto(source.getRecordType());

      List<FieldItem> fields = marcRecord.getControlFields().stream().map(cf -> controlFieldToQuickMarcField(cf, marcRecord.getLeader(), marcFormat)).collect(Collectors.toList());

      fields.addAll(marcRecord.getDataFields().stream().map(field -> dataFieldToQuickMarcField(field, marcRecord.getLeader(), marcFormat)).collect(Collectors.toList()));

      return new QuickMarc().parsedRecordId(parsedRecord.getId()).leader(leader).fields(fields).parsedRecordDtoId(source.getId()).externalId(getExternalId(source)).externalHrid(getExternalHrId(source)).marcFormat(supportedType()).suppressDiscovery(source.getAdditionalInfo().getSuppressDiscovery()).updateInfo(new UpdateInfo().recordState(UpdateInfo.RecordStateEnum.fromValue(source.getRecordState().getValue())).updateDate(convertDate(source)));
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private MarcFormat supportedType() {
    return null;
  }

  protected UUID getExternalId(ParsedRecordDto source) {
    return null;
  }

  ;

  protected String getExternalHrId(ParsedRecordDto source) {
    return null;
  }

  ;

  private OffsetDateTime convertDate(ParsedRecordDto parsedRecordDto) {
    var updatedDate = parsedRecordDto.getMetadata().getUpdatedDate();
    return updatedDate != null ? OffsetDateTime.ofInstant(updatedDate.toInstant(), ZoneId.systemDefault()) : null;
  }

  private FieldItem dataFieldToQuickMarcField(DataField field, Leader leader, MarcFormat marcFormat) {
    return dataFieldConverters.stream().filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst().map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"));
  }

  private FieldItem controlFieldToQuickMarcField(ControlField field, Leader leader, MarcFormat marcFormat) {
    return controlFieldConverters.stream().filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst().map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"))
      .indicators(Collections.emptyList());
  }

  private String masqueradeBlanks(String sourceString) {
    return sourceString.replace(SPACE, BLANK_REPLACEMENT);
  }

}
