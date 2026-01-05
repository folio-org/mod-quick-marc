package org.folio.qm.convertion.field.qm;

import static org.folio.qm.util.MarcUtils.extractSubfields;

import org.folio.qm.convertion.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.DataFieldImpl;

public abstract class AbstractFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    var dataField = new DataFieldImpl(field.getTag(), getIndicator(field, 0), getIndicator(field, 1));
    dataField.getSubfields().addAll(extractSubfields(field, this::subfieldFromString));
    return dataField;
  }

  protected abstract Subfield subfieldFromString(String string);
}
