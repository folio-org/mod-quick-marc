package org.folio.qm.converter;

import static org.folio.qm.converter.elements.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.MarcUtils.encodeToMarcDateTime;
import static org.folio.qm.util.MarcUtils.getFieldByTag;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.model.AdditionalInfo;
import org.folio.qm.client.model.ParsedRecord;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapper;
import org.folio.qm.util.QmMarcJsonWriter;
import org.jspecify.annotations.NonNull;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

@Component
@RequiredArgsConstructor
public class MarcQmConverter<T extends BaseMarcRecord> implements Converter<T, ParsedRecordDto> {

  private final ObjectMapper objectMapper;
  private final MarcTypeMapper typeMapper;
  private final Converter<BaseMarcRecord, Record> recordConverter;

  @Override
  public ParsedRecordDto convert(@NonNull T source) {
    var format = source.getMarcFormat();
    return new ParsedRecordDto()
      .setRecordType(typeMapper.toDto(format))
      .setParsedRecord(new ParsedRecord(convertToParsedContent(source)))
      .setAdditionalInfo(new AdditionalInfo(source.getSuppressDiscovery()));
  }

  protected static <T extends BaseMarcRecord> T updateRecordTimestamp(T quickMarc) {
    final var currentTime = encodeToMarcDateTime(LocalDateTime.now());
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresentOrElse(field -> field.setContent(currentTime),
        () -> quickMarc.addFieldsItem(
          new FieldItem().tag(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD).content(currentTime))
      );
    return quickMarc;
  }

  private JsonNode convertToParsedContent(T source) {
    var marcRecord = recordConverter.convert(source);
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
         QmMarcJsonWriter writer = new QmMarcJsonWriter(os)) {
      writer.write(marcRecord);

      var convertedContent = objectMapper.readTree(os.toByteArray());
      reorderContentTagsBasedOnSource(convertedContent, source.getFields());
      return convertedContent;
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private void reorderContentTagsBasedOnSource(JsonNode convertedContent, List<FieldItem> sourceFields) {
    var fieldsArrayNode = (ArrayNode) convertedContent.path("fields");

    Map<String, Queue<JsonNode>> jsonNodesByTag = new HashMap<>();
    fieldsArrayNode.forEach(node -> {
      String tag = node.propertyNames().iterator().next();
      jsonNodesByTag.computeIfAbsent(tag, k -> new LinkedList<>()).add(node);
    });

    var rearrangedArray = objectMapper.createArrayNode();
    for (FieldItem fieldItem : sourceFields) {
      Queue<JsonNode> nodes = jsonNodesByTag.get(fieldItem.getTag());
      if (nodes != null && !nodes.isEmpty()) {
        rearrangedArray.add(nodes.poll());
      }
    }

    fieldsArrayNode.removeAll();
    fieldsArrayNode.addAll(rearrangedArray);
  }
}
