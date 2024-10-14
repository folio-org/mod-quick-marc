package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.Constants.LCCN_CONTROL_FIELD;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.stereotype.Component;

@Component
public class Tag010FieldItemConverter extends AbstractFieldItemConverter {

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(LCCN_CONTROL_FIELD);
  }

  @Override
  protected Subfield subfieldFromString(String string) {
    return new SubfieldImpl(string.charAt(1), string.substring(2));
  }
}
