package org.folio.converter;

import static org.folio.converter.StringConstants.BLVL;
import static org.folio.converter.StringConstants.CONTENT;
import static org.folio.converter.StringConstants.TYPE;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParsedRecordToQuickMarcConverter implements Converter<ParsedRecord, QuickMarcJson> {
  @Override
  public QuickMarcJson convert(ParsedRecord parsedRecord) {
    InputStream input = IOUtils.toInputStream(JsonObject.mapFrom(parsedRecord).encode(), StandardCharsets.UTF_8);
    MarcReader reader = new MarcJsonReader(input);
    Record marcRecord = reader.next();

    QuickMarcJson quickMarcJson = new QuickMarcJson();
    quickMarcJson.setParsedRecordId(parsedRecord.getId());

    String leader = marcRecord.getLeader().marshal();
    quickMarcJson.setLeader(leader);

    marcRecord.getControlFields().forEach(controlField -> {
      Field field = new Field();
      field.setTag(controlField.getTag());
      field.setContent(("008".equals(controlField.getTag()))? splitField008(controlField.getData(), leader): controlField.getData());
      quickMarcJson.getFields().add(field);
    });

    marcRecord.getDataFields().forEach(dataField -> quickMarcJson.getFields().add(dataFieldToQuickMarcField(dataField)));

    return quickMarcJson;
  }

  private Map splitField008(String content, String leader){
    String type = leader.substring(6, 7);
    String bLvl = leader.substring(7, 8);
    ContentType contentType = ContentType.resolveContentType(type, bLvl);

    Map<String, Object> map = new LinkedHashMap<>();
    map.put(CONTENT, contentType.getName());
    map.put(TYPE, type);
    map.put(BLVL, bLvl);
    map.putAll(Field008SplitterFactory.getStrategy(contentType).apply(content));

    return map;
  }

  private Field dataFieldToQuickMarcField(DataField dataField) {
    Field field = new Field();
    field.setTag(dataField.getTag());
    field.setContent(subfieldsToString(dataField));
    String[] indicators = {
      Character.toString(dataField.getIndicator1()),
      Character.toString(dataField.getIndicator2())
    };
    field.setIndicators(Arrays.asList(indicators));
    return field;
  }

  private String subfieldsToString(DataField dataField){
    return dataField.getSubfields().stream()
      .map(subfield -> new StringBuilder("$").append(subfield.getCode()).append(" ").append(subfield.getData()))
      .collect(Collectors.joining(" "));
  }
}
