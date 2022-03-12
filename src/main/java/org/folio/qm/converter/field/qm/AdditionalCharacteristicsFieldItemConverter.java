package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.AdditionalMaterialConfiguration.resolveByCode;
import static org.folio.qm.converter.elements.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH;

import java.util.Map;

import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class AdditionalCharacteristicsFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(),
      restoreBlanks(restoreControlField((Map<String, Object>) field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD);
  }

  private String restoreControlField(Map<String, Object> contentMap) {
    var type = contentMap.get(ControlFieldItem.TYPE.getName()).toString();
    var configuration = resolveByCode(type.charAt(0));
    return restoreFixedLengthField(ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH, configuration.getControlFieldItems(),
      contentMap, 0);
  }

}
