package org.folio.qm.converternew.qm;

import static org.folio.qm.converter.elements.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.BIBLIOGRAPHIC_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.BLVL;
import static org.folio.qm.converter.elements.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_END_INDEX;

import java.util.Map;

import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.converter.elements.MaterialTypeConfiguration;
import org.folio.qm.converternew.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class GeneralInformationBibliographicFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(),
      restoreBlanks(restoreControlField((Map<String, Object>) field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(GENERAL_INFORMATION_CONTROL_FIELD) && marcFormat == MarcFormat.BIBLIOGRAPHIC;
  }

  protected String restoreControlField(Map<String, Object> contentMap) {
    var type = contentMap.get(ControlFieldItem.TYPE.getName()).toString().charAt(0);
    var blvl = contentMap.get(BLVL).toString().charAt(0);

    var materialTypeConfiguration = MaterialTypeConfiguration.resolveContentType(type, blvl);
    String specificItemsString = restoreFixedLengthField(ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH - 1,
      materialTypeConfiguration.getControlFieldItems(), contentMap, -1);
    return new StringBuilder(
      restoreFixedLengthField(BIBLIOGRAPHIC_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH,
        MaterialTypeConfiguration.getCommonItems(),
        contentMap, -1))
      .replace(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX, specificItemsString).toString();
  }
}
