package org.folio.converter;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.folio.converter.ContentType.UNKNOWN;
import static org.folio.converter.FixedLengthControlFieldItems.VALUE;
import static org.folio.util.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD;
import static org.folio.util.Constants.BLVL;
import static org.folio.util.Constants.DESC;
import static org.folio.util.Constants.ELVL;
import static org.folio.util.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.util.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;
import static org.folio.util.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.util.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.util.Constants.TYPE;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;

@Component
public class ParsedRecordToQuickMarcConverter implements Converter<ParsedRecord, QuickMarcJson> {
  private ContentType contentType;

  @Override
  public QuickMarcJson convert(ParsedRecord parsedRecord) {
    InputStream input = IOUtils.toInputStream(JsonObject.mapFrom(parsedRecord).encode(), StandardCharsets.UTF_8);
    Record marcRecord = new MarcJsonReader(input).next();

    Leader leader = marcRecord.getLeader();
    contentType = ContentType.resolveContentType(leader.getTypeOfRecord());
    List<Field> fields = new ArrayList<>();
    marcRecord.getControlFields().forEach(controlField ->
      fields.add(new Field()
        .withTag(controlField.getTag())
        .withContent(processControlField(controlField, leader)))
    );

    fields.addAll(marcRecord.getDataFields().stream()
      .map(this::dataFieldToQuickMarcField)
      .collect(Collectors.toList()));

    return new QuickMarcJson()
      .withParsedRecordId(parsedRecord.getId())
      .withLeader(leader.marshal())
      .withFields(fields);
  }

  private Object processControlField(ControlField controlField, Leader leader) {
    switch (controlField.getTag()) {
      case ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD:
        return splitAdditionalCharacteristicsControlField(controlField.getData());
      case PHYSICAL_DESCRIPTIONS_CONTROL_FIELD:
        return splitPhysicalDescriptionsControlField(controlField.getData());
      case GENERAL_INFORMATION_CONTROL_FIELD:
        return splitGeneralInformationControlField(controlField.getData(), leader);
      default:
        return controlField.getData();
    }
  }

  private Object splitAdditionalCharacteristicsControlField(String content) {
    if (contentType.equals(UNKNOWN)) {
      return Collections.singletonMap(VALUE.getName(), content);
    }
    return fillContentMap(contentType.getFixedLengthControlFieldItems(), content.substring(1));
  }

  private Map<String, Object> splitGeneralInformationControlField(String content, Leader leader){
    Map<String, Object> fieldItems = new LinkedHashMap<>();
    fieldItems.put(TYPE, leader.getTypeOfRecord());
    fieldItems.put(BLVL, leader.getImplDefined1()[0]);
    fieldItems.put(ELVL, leader.getImplDefined2()[0]);
    fieldItems.put(DESC, leader.getImplDefined2()[1]);
    fieldItems.putAll(fillContentMap(ContentType.getCommonItems(), content));
    fieldItems.putAll(fillContentMap(contentType.getFixedLengthControlFieldItems(), content.substring(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX)));
    return fieldItems;
  }

  private Map<String, Object> splitPhysicalDescriptionsControlField(String content) {
    PhysicalDescriptions physicalDescriptions = PhysicalDescriptions.resolveByCode(content.charAt(0));
    Map<String, Object> fieldItems = new LinkedHashMap<>();
    physicalDescriptions.getItems().forEach(item -> {
      String value = (item.getLength() != 0) ? content.substring(item.getPosition(), item.getPosition() + item.getLength()) : content;
      fieldItems.put(item.getName(), value);
    });
    return fieldItems;
  }

  private Map<String, Object> fillContentMap(List<FixedLengthControlFieldItems> items, String content) {
    Map<String, Object> fieldItems = new LinkedHashMap<>();
    items.forEach(item -> {
      String value = content.substring(item.getPosition(), item.getPosition() + item.getLength());
      fieldItems.put(item.getName(), item.isArray() ? Arrays.asList(value.split(EMPTY)) : value);
    });
    return fieldItems;
  }

  private Field dataFieldToQuickMarcField(DataField dataField) {
    return new Field()
      .withTag(dataField.getTag())
      .withIndicators(Arrays.asList(Character.toString(dataField.getIndicator1()), Character.toString(dataField.getIndicator2())))
      .withContent(dataField.getSubfields().stream()
        .map(subfield -> new StringBuilder("$").append(subfield.getCode()).append(SPACE).append(subfield.getData()))
        .collect(Collectors.joining(SPACE)));
  }
}
