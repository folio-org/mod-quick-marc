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
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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
  private final ObjectMapper objectMapper;
  private final MarcTypeMapper typeMapper;
  private final MarcFieldsConverter fieldsConverter;

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarc source) {
    var format = source.getMarcFormat();
    var content = convertToParsedContent(source, format);
    return new ParsedRecordDto()
      .id(source.getParsedRecordDtoId())
      .recordType(typeMapper.toDto(format))
      .externalIdsHolder(convertExternalIdsHolder(source))
      .relatedRecordVersion(source.getRelatedRecordVersion())
      .parsedRecord(new ParsedRecord().id(source.getParsedRecordId()).content(content))
      .additionalInfo(new AdditionalInfo().suppressDiscovery(source.getSuppressDiscovery()));
  }

  private JsonNode convertToParsedContent(QuickMarc source, MarcFormat format) {
    var marcRecord = toMarcRecord(source.getLeader(), source.getFields(), format);
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
         QmMarcJsonWriter writer = new QmMarcJsonWriter(os)) {
      writer.write(marcRecord);
      return objectMapper.readTree(os.toByteArray());
    } catch (IOException e) {
      throw new ConverterException(e);
    }
  }

  protected ExternalIdsHolder convertExternalIdsHolder(QuickMarc quickMarc) {
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

  private Record toMarcRecord(String leaderString, List<FieldItem> fields, MarcFormat format) {
    var record = factory.newRecord();
    fieldsConverter.convertQmFields(fields, format)
      .forEach(record::addVariableField);
    record.setLeader(convertLeader(leaderString));
    return record;
  }

  private Leader convertLeader(String leaderString) {
    return factory.newLeader(restoreBlanks(leaderString));
  }

}
