package org.folio.converter;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.folio.util.Constants.BLVL;
import static org.folio.util.Constants.CONTENT;
import static org.folio.util.Constants.FIXED_LENGTH_CONTROL_FIELD;
import static org.folio.util.Constants.TYPE;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;

@Component
public class ParsedRecordToQuickMarcConverter implements Converter<ParsedRecord, QuickMarcJson> {

  @Override
  public QuickMarcJson convert(@NonNull ParsedRecord parsedRecord) {
    InputStream input = IOUtils.toInputStream(JsonObject.mapFrom(parsedRecord).encode(), StandardCharsets.UTF_8);
    Record marcRecord = new MarcJsonReader(input).next();

    Leader leader = marcRecord.getLeader();
    List<Field> fields = new ArrayList<>();
    marcRecord.getControlFields().forEach(controlField ->
      fields.add(new Field()
        .withTag(controlField.getTag())
        .withContent((FIXED_LENGTH_CONTROL_FIELD.equals(controlField.getTag())) ? splitFixedLengthControlField(controlField.getData(), leader) : controlField.getData()))
    );

    fields.addAll(marcRecord.getDataFields().stream()
      .map(this::dataFieldToQuickMarcField)
      .collect(Collectors.toList()));

    return new QuickMarcJson()
      .withParsedRecordId(parsedRecord.getId())
      .withLeader(leader.marshal())
      .withFields(fields);
  }

  private Map<String, Object> splitFixedLengthControlField(String content, Leader leader){
    ContentType contentType = ContentType.resolveContentType(leader);
    Map<String, Object> fieldItems = new LinkedHashMap<>();
    fieldItems.put(CONTENT, contentType.getName());
    fieldItems.put(TYPE, leader.getTypeOfRecord());
    fieldItems.put(BLVL, leader.getImplDefined1()[0]);
    contentType.getFixedLengthControlFieldItems().forEach(item -> {
      String value = content.substring(item.getPosition(), item.getPosition() + item.getLength());
      fieldItems.put(item.getName(), item.isArray() ? Arrays.asList(value.split(StringUtils.EMPTY)) : value);

    });
    return fieldItems;
  }

  private Field dataFieldToQuickMarcField(DataField dataField) {
    return new Field()
      .withTag(dataField.getTag())
      .withIndicators(Arrays.asList(Character.toString(dataField.getIndicator1()), Character.toString(dataField.getIndicator2())))
      .withContent(dataField.getSubfields().stream()
        .map(Object::toString)
        .collect(Collectors.joining(SPACE)));
  }
}
