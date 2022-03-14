package org.folio.qm.converter.field.dto;

import static org.folio.qm.converter.elements.AdditionalMaterialConfiguration.resolveByCode;
import static org.folio.qm.converter.elements.Constants.TAG_006_CONTROL_FIELD;
import static org.folio.qm.util.MarcUtils.masqueradeBlanks;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class Tag006ControlFieldConverter implements VariableFieldConverter<ControlField> {

  @Override
  public FieldItem convert(ControlField field, Leader leader) {
    var content = masqueradeBlanks(field.getData());
    var configuration = resolveByCode(content.charAt(0));
    var contentMap = fillContentMap(configuration.getControlFieldItems(), content, 0);
    return new FieldItem().tag(field.getTag()).content(contentMap);
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof ControlField && field.getTag().equals(TAG_006_CONTROL_FIELD);
  }
}
