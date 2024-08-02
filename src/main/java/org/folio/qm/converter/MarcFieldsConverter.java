package org.folio.qm.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.converter.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecord;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcFieldsConverter {

  public static final String TAG_REGEX = "\\{(\\d{3})=([^}]+)";
  private final List<FieldItemConverter> fieldItemConverters;
  private final List<VariableFieldConverter<DataField>> dataFieldConverters;
  private final List<VariableFieldConverter<ControlField>> controlFieldConverters;

  public List<VariableField> convertQmFields(List<FieldItem> fields, MarcFormat format) {
    return fields.stream()
      .map(field -> toVariableField(field, format, false))
      .toList();
  }

  /**
   * Convert Quick Marc fields in soft mode, ignoring invalid data.
   * */
  public List<VariableField> convertQmFieldsSoft(List<FieldItem> fields, MarcFormat format) {
    return fields.stream()
      .map(field -> toVariableField(field, format, true))
      .toList();
  }

  public List<FieldItem> convertDtoFields(List<VariableField> fields, Leader leader, MarcFormat marcFormat) {
    var controlFields = fields.stream()
      .filter(ControlField.class::isInstance)
      .map(cf -> controlFieldToQuickMarcField((ControlField) cf, leader, marcFormat));
    var dataFields = fields.stream()
      .filter(DataField.class::isInstance)
      .map(field -> dataFieldToQuickMarcField((DataField) field, leader, marcFormat));
    return Stream.concat(controlFields, dataFields).toList();
  }

  public VariableField toVariableField(FieldItem field, MarcFormat marcFormat, boolean soft) {
    return fieldItemConverters.stream()
      .filter(fieldItemConverter -> fieldItemConverter.canProcess(field, marcFormat))
      .findFirst()
      .map(fieldItemConverter -> fieldItemConverter.convert(field, soft))
      .orElseThrow(() -> new IllegalArgumentException("Field converter not found"));
  }

  private FieldItem controlFieldToQuickMarcField(ControlField field, Leader leader, MarcFormat marcFormat) {
    return controlFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No control field converter found"))
      .indicators(Collections.emptyList());
  }

  private FieldItem dataFieldToQuickMarcField(DataField field, Leader leader, MarcFormat marcFormat) {
    return dataFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"));
  }

  public List<FieldItem> reorderFieldsBasedOnParsedRecordOrder(List<FieldItem> fieldItems, ParsedRecord parsedRecord) {
    var parsedRecordTags =  extractTagsFromParsedRecord(parsedRecord);
    return reorderMarcRecordFields(fieldItems, parsedRecordTags);
  }

  private List<String> extractTagsFromParsedRecord(ParsedRecord parsedRecord) {
    List<String> tagList = new ArrayList<>();

    var pattern = Pattern.compile(TAG_REGEX);
    var matcher = pattern.matcher(parsedRecord.getContent().toString());

    while (matcher.find()) {
      String tag = matcher.group(1);
      tagList.add(tag);
    }
    return tagList;
  }

  private List<FieldItem> reorderMarcRecordFields(List<FieldItem> fields, List<String> parsedRecordTags) {
    List<FieldItem> reorderedFields = new ArrayList<>();

    Map<String, Queue<FieldItem>> fieldItemQueueByTag = new HashMap<>();
    for (FieldItem fieldItem : fields) {
      fieldItemQueueByTag.computeIfAbsent(fieldItem.getTag(), k -> new LinkedList<>()).add(fieldItem);
    }

    for (String tag : parsedRecordTags) {
      Queue<FieldItem> fieldItemQueue = fieldItemQueueByTag.get(tag);
      if (fieldItemQueue != null && !fieldItemQueue.isEmpty()) {
        reorderedFields.add(fieldItemQueue.poll());
      }
    }

    return reorderedFields;
  }
}
