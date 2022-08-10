package org.folio.qm.converter.field.qm;

import static org.folio.qm.converter.elements.Constants.BLVL;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.qm.converter.elements.Constants.TAG_006_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.TAG_008_BIBLIOGRAPHIC_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.TAG_008_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Tag008Configuration.getCommonItems;
import static org.folio.qm.util.MarcUtils.restoreBlanks;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.converter.elements.Tag008Configuration;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

@Component
public class Tag008BibliographicFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(), restoreBlanks(restoreControlField(field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(TAG_008_CONTROL_FIELD) && marcFormat == MarcFormat.BIBLIOGRAPHIC;
  }

  private String restoreControlField(@NotNull Object content) {
    @SuppressWarnings("unchecked")
    var contentMap = (Map<String, Object>) content;
    var type = contentMap.get(ControlFieldItem.TYPE.getName()).toString().charAt(0);
    var blvl = contentMap.get(BLVL).toString().charAt(0);

    var itemConfig = Tag008Configuration.resolveContentType(type, blvl);
    var specificItemsString = getSpecificItemsString(contentMap, itemConfig);
    var commonItemsString = getCommonItemsString(contentMap);
    return new StringBuilder(commonItemsString)
      .replace(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX, specificItemsString).toString();
  }

  private String getSpecificItemsString(Map<String, Object> contentMap, Tag008Configuration itemConfig) {
    return restoreFixedLengthField(contentMap, TAG_006_CONTROL_FIELD_LENGTH - 1, -1,
      itemConfig.getSpecificItems());
  }

  private String getCommonItemsString(Map<String, Object> contentMap) {
    return restoreFixedLengthField(contentMap, TAG_008_BIBLIOGRAPHIC_CONTROL_FIELD_LENGTH, -1, getCommonItems());
  }
}
