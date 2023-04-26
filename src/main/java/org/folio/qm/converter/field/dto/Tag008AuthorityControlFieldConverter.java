package org.folio.qm.converter.field.dto;

import static org.folio.qm.converter.elements.Constants.AUTHORITY_CONTROL_FIELD_ITEMS;
import static org.folio.qm.converter.elements.Constants.TAG_008_AUTHORITY_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.TAG_008_CONTROL_FIELD;
import static org.folio.qm.util.MarcUtils.normalizeFixedLengthString;

import java.util.LinkedHashMap;
import org.folio.qm.converter.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

@Component
public class Tag008AuthorityControlFieldConverter implements VariableFieldConverter<ControlField> {

  @Override
  public FieldItem convert(ControlField field, Leader leader) {
    var content = normalizeFixedLengthString(field.getData(), TAG_008_AUTHORITY_CONTROL_FIELD_LENGTH);
    var contentMap = new LinkedHashMap<>(fillContentMap(AUTHORITY_CONTROL_FIELD_ITEMS, content, 0));
    return new FieldItem().tag(field.getTag()).content(contentMap);
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof ControlField
      && field.getTag().equals(TAG_008_CONTROL_FIELD)
      && marcFormat == MarcFormat.AUTHORITY;
  }

}
