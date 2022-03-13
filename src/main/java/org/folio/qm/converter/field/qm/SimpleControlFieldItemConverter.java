package org.folio.qm.converter.field.qm;

import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class SimpleControlFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(), field.getContent().toString());
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return isSimpleControlField(field);
  }
}
