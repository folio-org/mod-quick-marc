package org.folio.converter;

import static org.folio.converter.StringConstants.CONTENT;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
  public ParsedRecord convert(QuickMarcJson quickMarcJson) {
    Record marcRecord = quickMarcJsonToMarcRecord(quickMarcJson);

    Map<String, Object> contentMap = new LinkedHashMap<>();
    contentMap.put("fields", extractFields(marcRecord));
    contentMap.put("leader", marcRecord.getLeader().marshal());

    ParsedRecord parsedRecord = new ParsedRecord()
      .withId(quickMarcJson.getId())
      .withContent(contentMap);

    return parsedRecord;
  }

  private Record quickMarcJsonToMarcRecord(QuickMarcJson quickMarcJson){
    Record marcRecord = factory.newRecord();
    marcRecord.setLeader(factory.newLeader(quickMarcJson.getLeader()));
    quickMarcJson.getFields().forEach(field -> {
      String tag = field.getTag();
      String data = field.getContent().toString();
      if (field.getIndicators().size() == 0) {
        ControlField controlField = factory.newControlField();
        controlField.setTag(tag);
        if ("008".equals(tag)) {
          controlField.setData(restoreField008((LinkedHashMap) field.getContent()));
        } else {
          controlField.setData(data);
        }
        marcRecord.getControlFields().add(controlField);
      } else {
        DataField dataField = factory.newDataField();
        dataField.setTag(field.getTag());
        dataField.getSubfields().addAll(stringToSubfields(field.getContent().toString()));
        dataField.setIndicator1((field.getIndicators().get(0) == null)? ' ': field.getIndicators().get(0).toString().charAt(0));
        dataField.setIndicator2((field.getIndicators().get(1) == null)? ' ': field.getIndicators().get(1).toString().charAt(0));
        marcRecord.getDataFields().add(dataField);
      }
    });
    return marcRecord;
  }

  private String restoreField008(Map map) {
    ContentType contentType = ContentType.getByName(map.get(CONTENT).toString());
    String result = Field008RestoreFactory.getStrategy(contentType).apply(map);
    if (result.length() != ITEM008_LENGTH) {
      throw new IllegalArgumentException("Field 008 must be 40 characters in length");
    }
    return result;
  }

  private List<Subfield> stringToSubfields(String s){
    List<Subfield> result = new ArrayList<>();
    stringToTokens(s).forEach(token -> {
      Subfield subfield = factory.newSubfield();
      subfield.setCode(token.charAt(0));
      subfield.setData((token.charAt(1) == ' ')? token.substring(2): token.substring(1));
      result.add(subfield);
    });
    return result;
  }

  private List<String> stringToTokens(String s) {
    List<String> result = new ArrayList<>();
    String[] tokens = s.split("[$]");
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i];
      if ((token.charAt(token.length() - 1) == ' ') && (i < tokens.length - 1)) {
        token = token.substring(0, token.length() - 1);
      }
      result.add(token);
    }
    return result;
  }

  private List<Object> extractFields(Record marcRecord) {
    List<Object> fields = new ArrayList<>();
    marcRecord.getControlFields().forEach(controlField ->
      fields.add(Collections.singletonMap(controlField.getTag(), controlField.getData())));
    marcRecord.getDataFields().forEach(dataField -> {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("ind1", Character.toString(dataField.getIndicator1()));
      map.put("ind2", Character.toString(dataField.getIndicator2()));
      map.put("subfields", dataField.getSubfields().stream()
        .map(subfield -> Collections.singletonMap(Character.toString(subfield.getCode()), subfield.getData()))
        .collect(Collectors.toList()));
      fields.add(Collections.singletonMap(dataField.getTag(), map));
    });
    return fields;
  }
}
