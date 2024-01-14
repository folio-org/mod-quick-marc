package org.folio.qm.converter.field.qm;

import static org.folio.qm.util.MarcUtils.extractSubfields;

import java.util.stream.Collectors;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.jetbrains.annotations.NotNull;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;

public abstract class AbstractFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    var tag = field.getTag();
    var data = getFieldData(field);
    return new ControlFieldImpl(tag, data);
  }

  @NotNull
  private String getFieldData(FieldItem field) {
    var indicator1 = String.valueOf(getIndicator(field, 0));
    var indicator2 = String.valueOf(getIndicator(field, 1));
    var indicators = indicator1 + indicator2;

    var content = extractSubfields(field, this::subfieldFromString).stream()
      .map(subfield -> "$" + subfield.getCode() + subfield.getData())
      .collect(Collectors.joining());

    return indicators + content;
  }

  protected abstract Subfield subfieldFromString(String string);
}
