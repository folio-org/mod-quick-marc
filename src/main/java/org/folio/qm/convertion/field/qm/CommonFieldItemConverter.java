package org.folio.qm.convertion.field.qm;

import static org.folio.qm.convertion.elements.Constants.LCCN_CONTROL_FIELD;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.stereotype.Component;

@Component
public class CommonFieldItemConverter extends AbstractFieldItemConverter {

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return !field.getTag().equals(LCCN_CONTROL_FIELD) && !isControlField(field);
  }

  @Override
  protected Subfield subfieldFromString(String string) {
    return new SubfieldImpl(string.charAt(1), string.substring(2).trim());
  }
}
