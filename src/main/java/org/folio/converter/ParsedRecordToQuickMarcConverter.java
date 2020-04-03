package org.folio.converter;

import static org.folio.converter.StringConstants.BLVL;
import static org.folio.converter.StringConstants.CONTENT;
import static org.folio.converter.StringConstants.EMPTY_STRING;
import static org.folio.converter.StringConstants.SPACE;
import static org.folio.converter.StringConstants.TAG_008;
import static org.folio.converter.StringConstants.TYPE;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.jetbrains.annotations.NotNull;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParsedRecordToQuickMarcConverter implements Converter<ParsedRecord, QuickMarcJson> {
  @Override
  public QuickMarcJson convert(@NotNull ParsedRecord parsedRecord) {
    InputStream input = IOUtils.toInputStream(JsonObject.mapFrom(parsedRecord).encode(), StandardCharsets.UTF_8);
    Record marcRecord = new MarcJsonReader(input).next();

    Leader leader = marcRecord.getLeader();
    List<Field> fields = new ArrayList<>();
    marcRecord.getControlFields().forEach(controlField ->
      fields.add(new Field()
        .withTag(controlField.getTag())
        .withContent((TAG_008.equals(controlField.getTag()))? splitField008(controlField.getData(), leader): controlField.getData())));

    fields.addAll(marcRecord.getDataFields().stream()
      .map(this::dataFieldToQuickMarcField)
      .collect(Collectors.toList()));

    return new QuickMarcJson()
      .withParsedRecordId(parsedRecord.getId())
      .withLeader(leader.marshal())
      .withFields(fields);
  }

  private Map<String, Object> splitField008(String content, Leader leader){
    ContentType contentType = ContentType.resolveContentType(leader);
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(CONTENT, contentType.getName());
    map.put(TYPE, leader.getTypeOfRecord());
    map.put(BLVL, leader.getImplDefined1()[0]);
    contentType.getField008Items().forEach(item -> {
      String val = content.substring(item.getPosition(), item.getPosition() + item.getLength());
      map.put(item.getName(), item.isArray()? Arrays.asList(val.split(EMPTY_STRING)): val);
    });
    return map;
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
