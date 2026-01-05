package org.folio.qm.convertion.field.dto;

import static org.folio.qm.convertion.elements.Constants.COMPLEX_CONTROL_FIELD_TAGS;

import org.folio.qm.convertion.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

@Component
public class CommonControlFieldConverter implements VariableFieldConverter<ControlField> {

  @Override
  public FieldItem convert(ControlField field, Leader leader) {
    return new FieldItem().tag(field.getTag()).content(field.getData());
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof ControlField && !COMPLEX_CONTROL_FIELD_TAGS.contains(field.getTag());
  }
}
