package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.Constants.AUTHORITY_CONTROL_FIELD_ITEMS;
import static org.folio.qm.converter.elements.Constants.TAG_008_AUTHORITY_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.TAG_008_CONTROL_FIELD;
import static org.folio.qm.converter.elements.ControlFieldItem.DATE_ENTERED;
import static org.folio.qm.util.MarcUtils.restoreBlanks;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

@Component
public class Tag008AuthorityFieldItemConverter extends Tag008FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(), restoreBlanks(restoreControlField(field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(TAG_008_CONTROL_FIELD) && marcFormat == MarcFormat.AUTHORITY;
  }

  private String restoreControlField(@NotNull Object content) {
    @SuppressWarnings("unchecked")
    var contentMap = (Map<String, Object>) content;
    setDateEntered(DATE_ENTERED.getName(), contentMap);

    return restoreFixedLengthField(contentMap, TAG_008_AUTHORITY_CONTROL_FIELD_LENGTH, 0,
      AUTHORITY_CONTROL_FIELD_ITEMS);
  }
}
