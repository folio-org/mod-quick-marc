package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.Constants.HOLDINGS_CONTROL_FIELD_ITEMS;
import static org.folio.qm.converter.elements.Constants.TAG_008_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_008_HOLDINGS_CONTROL_FIELD_LENGTH;
import static org.folio.qm.util.MarcUtils.restoreBlanks;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

@Component
public class Tag008HoldingsFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(), restoreBlanks(restoreControlField(field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(TAG_008_CONTROL_FIELD) && marcFormat == MarcFormat.HOLDINGS;
  }

  private String restoreControlField(@NotNull Object content) {
    @SuppressWarnings("unchecked")
    var contentMap = (Map<String, Object>) content;
    return restoreFixedLengthField(contentMap, TAG_008_HOLDINGS_CONTROL_FIELD_LENGTH, 0, HOLDINGS_CONTROL_FIELD_ITEMS);
  }
}
