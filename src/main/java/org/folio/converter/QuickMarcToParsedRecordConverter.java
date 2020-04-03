package org.folio.converter;

import static org.folio.converter.StringConstants.*;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.jetbrains.annotations.NotNull;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QuickMarcToParsedRecordConverter implements Converter<QuickMarcJson, ParsedRecord> {
  private static final int ITEM008_LENGTH = 40;

  private MarcFactory factory = new MarcFactoryImpl();

  @Override
  public ParsedRecord convert(@NotNull QuickMarcJson quickMarcJson) {
    Record marcRecord = quickMarcJsonToMarcRecord(quickMarcJson);

    Map<String, Object> contentMap = new LinkedHashMap<>();
    contentMap.put(FIELDS, fieldsToObjects(marcRecord));
    contentMap.put(LEADER, marcRecord.getLeader().marshal());

    return new ParsedRecord()
      .withId(quickMarcJson.getParsedRecordId())
      .withContent(contentMap);
  }

  @SuppressWarnings("unchecked")
  private Record quickMarcJsonToMarcRecord(QuickMarcJson quickMarcJson) {
    Record marcRecord = factory.newRecord();
    marcRecord.setLeader(factory.newLeader(quickMarcJson.getLeader()));
    quickMarcJson.getFields().forEach(field -> {
      String tag = field.getTag();
      String data = field.getContent().toString();
      if (field.getIndicators().isEmpty()) {
        ControlField controlField = factory.newControlField();
        controlField.setTag(tag);
        if (TAG_008.equals(tag)) {
          controlField.setData(restoreField008((LinkedHashMap<String, Object>) field.getContent()));
        } else {
          controlField.setData(data);
        }
        marcRecord.getControlFields().add(controlField);
      } else {
        DataField dataField = factory.newDataField();
        dataField.setTag(field.getTag());
        dataField.getSubfields().addAll(stringToSubfields(field.getContent().toString()));
        dataField.setIndicator1((field.getIndicators().get(0) == null)? SPACE_CHARACTER: field.getIndicators().get(0).toString().charAt(0));
        dataField.setIndicator2((field.getIndicators().get(1) == null)? SPACE_CHARACTER: field.getIndicators().get(1).toString().charAt(0));
        marcRecord.getDataFields().add(dataField);
      }
    });
    return marcRecord;
  }

  @SuppressWarnings("unchecked")
  private String restoreField008(Map<String, Object> map) {
    ContentType contentType = ContentType.getByName(map.get(CONTENT).toString());
    StringBuilder stringBuilder = new StringBuilder(EMPTY_STRING_008);
    contentType.getField008Items().forEach(item -> stringBuilder.replace(item.getPosition(), item.getPosition()+item.getLength(),
      item.isArray()? String.join(EMPTY_STRING, ((List<String>) map.get(item.getName()))) :
        map.get(item.getName()).toString()));
    String result = stringBuilder.toString();
    if (result.length() != ITEM008_LENGTH) {
      throw new IllegalArgumentException("Field 008 must be 40 characters in length");
    }
    return result;
  }

  private List<Subfield> stringToSubfields(String s) {
    List<Subfield> subfields = new ArrayList<>();
    Arrays.asList(s.split(SPLIT_PATTERN)).forEach(substring -> {
      if (!substring.isEmpty()){
        subfields.add(new SubfieldImpl(substring.charAt(0), substring.substring(1)));
      }
    });
    return subfields;
  }

  private List<Object> fieldsToObjects(Record marcRecord) {
    List<Object> fields = new ArrayList<>();
    marcRecord.getControlFields().forEach(controlField ->
      fields.add(Collections.singletonMap(controlField.getTag(), controlField.getData())));
    marcRecord.getDataFields().forEach(dataField -> {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put(INDICATOR1, Character.toString(dataField.getIndicator1()));
      map.put(INDICATOR2, Character.toString(dataField.getIndicator2()));
      map.put(SUBFIELDS, dataField.getSubfields().stream()
        .map(subfield -> Collections.singletonMap(Character.toString(subfield.getCode()), subfield.getData()))
        .collect(Collectors.toList()));
      fields.add(Collections.singletonMap(dataField.getTag(), map));
    });
    return fields;
  }
}
