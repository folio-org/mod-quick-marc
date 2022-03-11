package org.folio.qm.converternew.qm;

import static org.folio.qm.converter.elements.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.HOLDINGS_CONTROL_FIELD_ITEMS;
import static org.folio.qm.converter.elements.Constants.HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;

import java.util.Map;

import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.springframework.stereotype.Component;

import org.folio.qm.converternew.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class GeneralInformationHoldingsFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    return new ControlFieldImpl(field.getTag(),
      restoreBlanks(restoreControlField((Map<String, Object>) field.getContent())));
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(GENERAL_INFORMATION_CONTROL_FIELD) && marcFormat == MarcFormat.HOLDINGS;
  }

  protected String restoreControlField(Map<String, Object> contentMap) {
    return restoreFixedLengthField(HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH, HOLDINGS_CONTROL_FIELD_ITEMS,
      contentMap, 0);
  }
}
