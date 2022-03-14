package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.Constants.TAG_007_CONTROL_FIELD;
import static org.folio.qm.converter.elements.ControlFieldItem.CATEGORY;
import static org.folio.qm.converter.elements.ControlFieldItem.VALUE;
import static org.folio.qm.converter.elements.PhysicalDescriptionFixedFieldElements.resolveByCode;
import static org.folio.qm.util.MarcUtils.restoreBlanks;

import java.util.Map;
import javax.validation.constraints.NotNull;

import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.PhysicalDescriptionFixedFieldElements;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class Tag007FieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(), restoreBlanks(restoreControlField(field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(TAG_007_CONTROL_FIELD);
  }

  private String restoreControlField(@NotNull Object content) {
    @SuppressWarnings("unchecked")
    var contentMap = (Map<String, Object>) content;
    char code = contentMap.get(CATEGORY.getName()).toString().charAt(0);
    var itemConfig = resolveByCode(code);
    if (itemConfig.equals(PhysicalDescriptionFixedFieldElements.UNKNOWN)) {
      return contentMap.get(VALUE.getName()).toString();
    } else {
      return restoreFixedLengthField(contentMap, itemConfig.getLength(), 0, itemConfig.getControlFieldItems());
    }
  }

}
