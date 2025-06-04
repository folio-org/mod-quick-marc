package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.Constants.TAG_006_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_006_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Tag006Configuration.resolveByCode;
import static org.folio.qm.util.MarcUtils.restoreBlanks;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

@Component
public class Tag006FieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(), restoreBlanks(restoreControlField(field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(TAG_006_CONTROL_FIELD);
  }

  private String restoreControlField(@NotNull Object content) {
    @SuppressWarnings("unchecked")
    var contentMap = (Map<String, Object>) content;
    var type = contentMap.get(ControlFieldItem.TYPE.getName()).toString();
    var configuration = resolveByCode(type.charAt(0));
    return restoreFixedLengthField(contentMap, TAG_006_CONTROL_FIELD_LENGTH, 0, configuration.getControlFieldItems());
  }
}
