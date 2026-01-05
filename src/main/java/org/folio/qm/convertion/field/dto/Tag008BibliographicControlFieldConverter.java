package org.folio.qm.convertion.field.dto;

import static org.folio.qm.convertion.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.convertion.elements.Constants.BLVL;
import static org.folio.qm.convertion.elements.Constants.SPACE_CHARACTER;
import static org.folio.qm.convertion.elements.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.convertion.elements.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.qm.convertion.elements.Constants.TAG_008_BIBLIOGRAPHIC_CONTROL_FIELD_LENGTH;
import static org.folio.qm.convertion.elements.Constants.TAG_008_CONTROL_FIELD;
import static org.folio.qm.convertion.elements.Constants.TYPE;
import static org.folio.qm.util.MarcUtils.normalizeFixedLengthString;

import java.util.LinkedHashMap;
import org.folio.qm.convertion.elements.Tag008Configuration;
import org.folio.qm.convertion.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

@Component
public class Tag008BibliographicControlFieldConverter implements VariableFieldConverter<ControlField> {

  @Override
  public FieldItem convert(ControlField field, Leader leader) {
    var typeOfRecord = leader.getTypeOfRecord();
    var implDefined1 = leader.getImplDefined1();
    var configuration = Tag008Configuration.resolveContentType(typeOfRecord, implDefined1[0]);

    var content = normalizeFixedLengthString(field.getData(), TAG_008_BIBLIOGRAPHIC_CONTROL_FIELD_LENGTH);
    var contentMap = new LinkedHashMap<>();
    contentMap.put(TYPE, Character.toString(typeOfRecord));
    contentMap.put(BLVL, change(implDefined1[0]));
    contentMap.putAll(fillContentMap(Tag008Configuration.getCommonItems(), content, -1));
    contentMap.putAll(fillContentMap(configuration.getSpecificItems(),
      content.substring(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX), -1));
    return new FieldItem().tag(field.getTag()).content(contentMap);
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof ControlField
      && field.getTag().equals(TAG_008_CONTROL_FIELD)
      && marcFormat == MarcFormat.BIBLIOGRAPHIC;
  }

  private String change(char c) {
    return c == SPACE_CHARACTER ? BLANK_REPLACEMENT : Character.toString(c);
  }
}
