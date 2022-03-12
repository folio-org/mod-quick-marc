package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;
import static org.folio.qm.converter.elements.ControlFieldItem.CATEGORY;
import static org.folio.qm.converter.elements.ControlFieldItem.VALUE;

import java.util.Map;

import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.PhysicalDescriptionFixedFieldElements;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class PhysicalMaterialFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(),
      restoreBlanks(restoreControlField((Map<String, Object>) field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(PHYSICAL_DESCRIPTIONS_CONTROL_FIELD);
  }

  private String restoreControlField(Map<String, Object> contentMap) {
    char code = contentMap.get(CATEGORY.getName()).toString().charAt(0);
    PhysicalDescriptionFixedFieldElements physicalDescriptionFixedFieldElements =
      PhysicalDescriptionFixedFieldElements.resolveByCode(code);
    if (physicalDescriptionFixedFieldElements.equals(PhysicalDescriptionFixedFieldElements.UNKNOWN)) {
      return contentMap.get(VALUE.getName()).toString();
    } else {
      return restoreFixedLengthField(physicalDescriptionFixedFieldElements.getLength(),
        physicalDescriptionFixedFieldElements.getControlFieldItems(), contentMap, 0);
    }
  }

}
