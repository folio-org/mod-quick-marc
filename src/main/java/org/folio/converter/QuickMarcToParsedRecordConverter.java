package org.folio.converter;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.util.Constants.CONTENT;
import static org.folio.util.Constants.FIXED_LENGTH_CONTROL_FIELD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class QuickMarcToParsedRecordConverter implements Converter<QuickMarcJson, ParsedRecord> {

  private static final int FIXED_LENGTH_CONTROL_FIELD_LENGTH = 40;
  private static final String FIELDS = "fields";
  private static final String LEADER = "leader";
  private static final String INDICATOR1 = "ind1";
  private static final String INDICATOR2 = "ind2";
  private static final String SUBFIELDS = "subfields";
  private static final String SPLIT_PATTERN = " ?[$]";
  private static final char SPACE_CHARACTER = ' ';

  private MarcFactory factory = new MarcFactoryImpl();

  @Override
  public ParsedRecord convert(@NonNull QuickMarcJson quickMarcJson) {
    Record marcRecord = quickMarcJsonToMarcRecord(quickMarcJson);

    Map<String, Object> contentMap = new LinkedHashMap<>();
    contentMap.put(FIELDS, convertMarcFieldsToObjects(marcRecord));
    contentMap.put(LEADER, marcRecord.getLeader().marshal());

    return new ParsedRecord()
      .withId(quickMarcJson.getParsedRecordId())
      .withContent(contentMap);
  }

  private Record quickMarcJsonToMarcRecord(QuickMarcJson quickMarcJson) {
    Record marcRecord = factory.newRecord();
    marcRecord.setLeader(factory.newLeader(quickMarcJson.getLeader()));
    quickMarcJson.getFields().forEach(field -> {
      String tag = field.getTag();
      if (field.getIndicators().isEmpty()) {
        ControlField controlField = factory.newControlField();
        controlField.setTag(tag);
        if (FIXED_LENGTH_CONTROL_FIELD.equals(tag)) {
          controlField.setData(restoreFixedLengthControlField((Map<String, Object>) field.getContent()));
        } else {
          controlField.setData(field.getContent().toString());
        }
        marcRecord.getControlFields().add(controlField);
      } else {
        DataField dataField = factory.newDataField();
        dataField.setTag(field.getTag());
        dataField.getSubfields().addAll(convertStringToSubfields(field.getContent().toString()));
        dataField.setIndicator1(retrieveIndicatorValue(field.getIndicators().get(0)));
        dataField.setIndicator2(retrieveIndicatorValue(field.getIndicators().get(1)));
        marcRecord.getDataFields().add(dataField);
      }
    });
    return marcRecord;
  }

  private char retrieveIndicatorValue(Object object) {
    return Objects.isNull(object) ? SPACE_CHARACTER : object.toString().charAt(0);
  }

  private String restoreFixedLengthControlField(Map<String, Object> map) {
    ContentType contentType = ContentType.getByName(map.get(CONTENT).toString());
    StringBuilder stringBuilder = new StringBuilder(StringUtils.repeat(SPACE_CHARACTER, FIXED_LENGTH_CONTROL_FIELD_LENGTH));
    contentType.getFixedLengthControlFieldItems().forEach(item -> stringBuilder.replace(item.getPosition(), item.getPosition() + item.getLength(),
      item.isArray() ? String.join(EMPTY, ((List<String>) map.get(item.getName()))) : map.get(item.getName()).toString()));
    String result = stringBuilder.toString();
    if (result.length() != FIXED_LENGTH_CONTROL_FIELD_LENGTH) {
      throw new IllegalArgumentException("Field 008 must be 40 characters in length");
    }
    return result;
  }

  private List<Subfield> convertStringToSubfields(String subfieldsString) {
    List<Subfield> subfields = new ArrayList<>();
    Arrays.asList(subfieldsString.split(SPLIT_PATTERN)).forEach(token -> {
      if (!token.isEmpty()){
        subfields.add(new SubfieldImpl(token.charAt(0), token.substring(1)));
      }
    });
    return subfields;
  }

  private List<Object> convertMarcFieldsToObjects(Record marcRecord) {
    List<Object> fields = new ArrayList<>();
    marcRecord.getControlFields().forEach(controlField ->
      fields.add(Collections.singletonMap(controlField.getTag(), controlField.getData())));
    marcRecord.getDataFields().forEach(dataField -> {
      Map<String, Object> fieldMap = new LinkedHashMap<>();
      fieldMap.put(INDICATOR1, Character.toString(dataField.getIndicator1()));
      fieldMap.put(INDICATOR2, Character.toString(dataField.getIndicator2()));
      fieldMap.put(SUBFIELDS, dataField.getSubfields().stream()
        .map(subfield -> Collections.singletonMap(Character.toString(subfield.getCode()), subfield.getData()))
        .collect(Collectors.toList()));
      fields.add(Collections.singletonMap(dataField.getTag(), fieldMap));
    });
    return fields;
  }
}
