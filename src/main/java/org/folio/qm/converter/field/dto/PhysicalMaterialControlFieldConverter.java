package org.folio.qm.converter.field.dto;

import static org.folio.qm.converter.elements.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;
import static org.folio.qm.converter.elements.PhysicalDescriptionFixedFieldElements.resolveByCode;

import java.util.LinkedHashMap;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.Constants;
import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.converter.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class PhysicalMaterialControlFieldConverter implements VariableFieldConverter<ControlField> {

  @Override
  public FieldItem convert(ControlField field, Leader leader) {
    var content = masqueradeBlanks(field.getData());
    var configuration = resolveByCode(content.charAt(0));
    var contentMap = new LinkedHashMap<>();
    contentMap.put(Constants.CATEGORY_NAME, configuration.getName());
    configuration.getControlFieldItems()
      .forEach(item -> contentMap.put(item.getName(), getControlFieldItemVal(content, item)));
    return new FieldItem().tag(field.getTag()).content(contentMap);
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof ControlField && field.getTag().equals(PHYSICAL_DESCRIPTIONS_CONTROL_FIELD);
  }

  private String getControlFieldItemVal(String content, ControlFieldItem element) {
    return element.getLength() != 0 ? extractElementFromContent(content, element, 0) : content;
  }
}
