package org.folio.converter;

import static org.folio.converter.StringConstants.BLVL;
import static org.folio.converter.StringConstants.CONTENT;
import static org.folio.converter.StringConstants.TYPE;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.Record;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;

@Component
public class RecordToQuickMarcConverter implements Converter<Record, QuickMarcJson> {
  @Override
  public QuickMarcJson convert(Record record) {
    ParsedRecord parsedRecord = record.getParsedRecord();
    InputStream input = IOUtils.toInputStream(JsonObject.mapFrom(parsedRecord).encode(), StandardCharsets.UTF_8);
    MarcReader reader = new MarcJsonReader(input);
    org.marc4j.marc.Record marcRecord = reader.next();

    QuickMarcJson quickMarcJson = new QuickMarcJson();
    quickMarcJson.setId(parsedRecord.getId());

    String leader = marcRecord.getLeader().marshal();
    String type = leader.substring(6, 7);
    String bLvl = leader.substring(7, 8);

    quickMarcJson.setLeader(leader);
    marcRecord.getControlFields().forEach(controlField -> {
      Field field = new Field();
      field.setTag(controlField.getTag());
      field.setContent(("008".equals(controlField.getTag()))? splitField008(controlField.getData(), type, bLvl): controlField.getData());
      quickMarcJson.getFields().add(field);
    });

    marcRecord.getDataFields().forEach(f -> quickMarcJson.getFields().add(dataFieldToQuickMarcField(f)));

    return quickMarcJson;
  }

  private JsonObject splitField008(String content, String type, String bLvl){
    ContentType contentType = ContentType.resolveContentType(type, bLvl);

    Map<String, Object> map = new LinkedHashMap<>();
    map.put(CONTENT, contentType.getName());
    map.put(TYPE, type);
    map.put(BLVL, bLvl);
    map.putAll(Field008SplitterFactory.getStrategy(contentType).apply(content));

    return JsonObject.mapFrom(map);
  }

  private Field dataFieldToQuickMarcField(DataField dataField) {
    Field field = new Field();
    field.setTag(dataField.getTag());
    field.setContent(subfieldsToLine(dataField));
    String[] indicators = {
      Character.toString(dataField.getIndicator1()),
      Character.toString(dataField.getIndicator2())
    };
    field.setIndicators(Arrays.asList(indicators));
    return field;
  }

  private String subfieldsToLine(DataField dataField){
    return dataField.getSubfields().stream()
      .map(subfield -> "$".concat(Character.toString(subfield.getCode())).concat(" ").concat(subfield.getData()))
      .collect(Collectors.joining(" "));
  }
}
