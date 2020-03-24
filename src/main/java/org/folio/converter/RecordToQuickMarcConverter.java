package org.folio.converter;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.RawRecord;
import org.folio.srs.model.Record;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecordToQuickMarcConverter implements Converter<Record, QuickMarcJson> {
  @Override
  public QuickMarcJson convert(Record record) {
    org.marc4j.marc.Record marcRecord = extractMarcRecord(record);

    String id = record.getRawRecord().getId();
    String leader = marcRecord.getLeader().toString();
    List<Field> fields = createQuickMarcFields(marcRecord);

    QuickMarcJson quickMarcJson = new QuickMarcJson();
    quickMarcJson.setId(id);
    quickMarcJson.setLeader(leader);
    quickMarcJson.setFields(fields);

    return quickMarcJson;
  }

  private org.marc4j.marc.Record extractMarcRecord(Record record) {
    RawRecord rawRecord = record.getRawRecord();

    InputStream inputStream = new ByteArrayInputStream(rawRecord.getContent().getBytes());
    MarcReader reader = new MarcStreamReader(inputStream);

    if (reader.hasNext()) return reader.next();
    else throw new RuntimeException("No content");
  }

  private List<Field> createQuickMarcFields(org.marc4j.marc.Record record) {
    String leader = record.getLeader().toString();
    String type = leader.substring(6, 7);
    String bLvl = leader.substring(7, 8);

    List<Field> result = record.getControlFields().stream()
      .map(controlField -> controlFieldToQuickMarcField(controlField, type, bLvl))
      .collect(Collectors.toList());

    result.addAll(record.getDataFields().stream()
      .map(this::dataFieldToQuickMarcField)
      .collect(Collectors.toList()));

    return result;
  }

  private Field controlFieldToQuickMarcField(ControlField controlField, String type, String bLvl) {
    Field field = new Field();

    String tag = controlField.getTag();
    field.setTag(tag);

    if("008".equals(tag)){
      field.setContent(splitField008(controlField.getData(), type, bLvl));
    } else {
      field.setContent(controlField.getData());
    }

    return field;
  }

  private String splitField008(String content, String type, String bLvl){
    JsonObject jsonObject = new JsonObject();

    ContentType contentType = ContentType.detectContentType(type, bLvl);

    jsonObject.put("Content", contentType.getName());
    jsonObject.put("Type", type);
    jsonObject.put("BLvl", bLvl);
    jsonObject.mergeIn(Field008SplitterFactory.getStrategy(contentType).split(content));

    return jsonObject.encodePrettily();
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
