package org.folio.qm.converter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.converter.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
@RequiredArgsConstructor
public class MarcFieldsConverter {

  private final List<FieldItemConverter> fieldItemConverters;
  private final List<VariableFieldConverter<DataField>> dataFieldConverters;
  private final List<VariableFieldConverter<ControlField>> controlFieldConverters;

  public List<VariableField> convertQmFields(List<FieldItem> fields, MarcFormat format) {
    return fields.stream()
      .map(field -> toVariableField(field, format))
      .collect(Collectors.toList());
  }

  public List<FieldItem> convertDtoFields(List<VariableField> fields, Leader leader, MarcFormat marcFormat) {
    var controlFields = fields.stream()
      .filter(field -> field instanceof ControlField)
      .map(cf -> controlFieldToQuickMarcField((ControlField) cf, leader, marcFormat));
    var dataFields = fields.stream()
      .filter(field -> field instanceof DataField)
      .map(field -> dataFieldToQuickMarcField((DataField) field, leader, marcFormat));
    return Stream.concat(controlFields, dataFields).collect(Collectors.toList());
  }

  public VariableField toVariableField(FieldItem field, MarcFormat marcFormat) {
    return fieldItemConverters.stream()
      .filter(fieldItemConverter -> fieldItemConverter.canProcess(field, marcFormat))
      .findFirst()
      .map(fieldItemConverter -> fieldItemConverter.convert(field))
      .orElseThrow(() -> new IllegalArgumentException("Field converter not found"));
  }

  private FieldItem controlFieldToQuickMarcField(ControlField field, Leader leader, MarcFormat marcFormat) {
    return controlFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"))
      .indicators(Collections.emptyList());
  }

  private FieldItem dataFieldToQuickMarcField(DataField field, Leader leader, MarcFormat marcFormat) {
    return dataFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"));
  }
}
