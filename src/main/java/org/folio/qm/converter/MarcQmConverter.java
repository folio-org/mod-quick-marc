package org.folio.qm.converter;

import static org.folio.qm.converter.elements.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.MarcUtils.encodeToMarcDateTime;
import static org.folio.qm.util.MarcUtils.getFieldByTag;
import static org.folio.qm.util.MarcUtils.restoreBlanks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.AdditionalInfo;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.util.QmMarcJsonWriter;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcQmConverter<T extends BaseMarcRecord> implements Converter<T, ParsedRecordDto> {

  private final MarcFactory factory;
  private final ObjectMapper objectMapper;
  private final MarcTypeMapper typeMapper;
  private final MarcFieldsConverter fieldsConverter;

  protected static <T extends BaseMarcRecord> T updateRecordTimestamp(T quickMarc) {
    final var currentTime = encodeToMarcDateTime(LocalDateTime.now());
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresentOrElse(field -> field.setContent(currentTime),
        () -> quickMarc.addFieldsItem(
          new FieldItem().tag(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD).content(currentTime))
      );
    return quickMarc;
  }

  @Override
  public ParsedRecordDto convert(@NonNull T source) {
    var format = source.getMarcFormat();
    return new ParsedRecordDto()
      .recordType(typeMapper.toDto(format))
      .parsedRecord(new ParsedRecord().content(convertToParsedContent(source, format)))
      .additionalInfo(new AdditionalInfo().suppressDiscovery(source.getSuppressDiscovery()));
  }

  private JsonNode convertToParsedContent(T source, MarcFormat format) {
    var marcRecord = toMarcRecord(source.getLeader(), source.getFields(), format);
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
         QmMarcJsonWriter writer = new QmMarcJsonWriter(os)) {
      writer.write(marcRecord);

      var convertedContent = objectMapper.readTree(os.toByteArray());
      reorderContentTagsBasedOnSource(convertedContent, source.getFields());
      return convertedContent;
    } catch (IOException e) {
      throw new ConverterException(e);
    }
  }

  private void reorderContentTagsBasedOnSource(JsonNode jsonNode, List<FieldItem> sourceFields) {
    var fieldsArrayNode = (ArrayNode) jsonNode.path("fields");

    Map<String, JsonNode> jsonNodesByTag = new HashMap<>();
    fieldsArrayNode
      .forEach(node -> jsonNodesByTag.put(node.fieldNames().next(), node));

    var rearrangedArray = objectMapper.createArrayNode();
    for (FieldItem fieldItem : sourceFields) {
      JsonNode node = jsonNodesByTag.get(fieldItem.getTag());
      if (node != null) {
        rearrangedArray.add(node);
      }
    }

    fieldsArrayNode.removeAll();
    fieldsArrayNode.addAll(rearrangedArray);
  }

  private Record toMarcRecord(String leaderString, List<FieldItem> fields, MarcFormat format) {
    var marcRecord = factory.newRecord();
    fieldsConverter.convertQmFields(fields, format)
      .forEach(marcRecord::addVariableField);
    marcRecord.setLeader(convertLeader(leaderString));
    return marcRecord;
  }

  private Leader convertLeader(String leaderString) {
    return factory.newLeader(restoreBlanks(leaderString));
  }
}
