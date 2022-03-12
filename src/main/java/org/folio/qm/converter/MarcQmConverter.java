package org.folio.qm.converter;

import static org.folio.qm.util.MarcUtils.restoreBlanks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.AdditionalInfo;
import org.folio.qm.domain.dto.ExternalIdsHolder;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.util.QmMarcJsonWriter;

@Component
@RequiredArgsConstructor
public class MarcQmConverter implements Converter<QuickMarc, ParsedRecordDto> {

  private final MarcFactory factory;
  private final List<FieldItemConverter> fieldItemConverters;
  private final MarcTypeMapper typeMapper;

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarc source) {
    try {
      JsonNode jsonNode = convertToJson(source);

      return new ParsedRecordDto()
        .id(source.getParsedRecordDtoId())
        .recordType(typeMapper.toDto(source.getMarcFormat()))
        .externalIdsHolder(constructExternalIdsHolder(source))
        .relatedRecordVersion(source.getRelatedRecordVersion())
        .parsedRecord(new ParsedRecord().id(source.getParsedRecordId()).content(jsonNode))
        .additionalInfo(new AdditionalInfo().suppressDiscovery(source.getSuppressDiscovery()));
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private JsonNode convertToJson(QuickMarc source) throws IOException {
    Record marcRecord = toMarcRecord(source);
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
         QmMarcJsonWriter writer = new QmMarcJsonWriter(os)) {
      writer.write(marcRecord);
      return new ObjectMapper().readTree(os.toByteArray());
    }
  }

  private Record toMarcRecord(QuickMarc quickMarcJson) {
    Record marcRecord = factory.newRecord();
    Leader leader = factory.newLeader(restoreBlanks(quickMarcJson.getLeader()));

    quickMarcJson.getFields()
      .stream()
      .map(field -> toVariableField(field, quickMarcJson.getMarcFormat()))
      .forEach(marcRecord::addVariableField);

    marcRecord.setLeader(leader);

    return marcRecord;
  }

  private VariableField toVariableField(FieldItem field, MarcFormat marcFormat) {
    return fieldItemConverters.stream()
      .filter(fieldItemConverter -> fieldItemConverter.canProcess(field, marcFormat))
      .findFirst()
      .map(fieldItemConverter -> fieldItemConverter.convert(field))
      .orElseThrow(() -> new IllegalArgumentException("Field converter not found"));
  }

  protected ExternalIdsHolder constructExternalIdsHolder(QuickMarc quickMarc) {
    var externalIdsHolder = new ExternalIdsHolder();
    switch (quickMarc.getMarcFormat()) {
      case BIBLIOGRAPHIC:
        externalIdsHolder.setInstanceId(quickMarc.getExternalId());
        externalIdsHolder.setInstanceHrid(quickMarc.getExternalHrid());
        break;
      case HOLDINGS:
        externalIdsHolder.setHoldingsId(quickMarc.getExternalId());
        externalIdsHolder.setHoldingsHrid(quickMarc.getExternalHrid());
        break;
      case AUTHORITY:
        externalIdsHolder.setAuthorityId(quickMarc.getExternalId());
        externalIdsHolder.setAuthorityHrid(quickMarc.getExternalHrid());
        break;
    }
    return externalIdsHolder;
  }

}
