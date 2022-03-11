package org.folio.qm.converternew.dto;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.elements.Constants.BLVL;
import static org.folio.qm.converter.elements.Constants.DESC;
import static org.folio.qm.converter.elements.Constants.ELVL;
import static org.folio.qm.converter.elements.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.SPACE_CHARACTER;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.qm.converter.elements.Constants.TYPE;

import java.util.LinkedHashMap;
import java.util.Map;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.MaterialTypeConfiguration;
import org.folio.qm.converternew.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class GeneralInformationBibliographicControlFieldConverter implements VariableFieldConverter<ControlField> {

  @Override
  public FieldItem convert(ControlField field, Leader leader) {
    var typeOfRecord = leader.getTypeOfRecord();
    var implDefined1 = leader.getImplDefined1();
    var implDefined2 = leader.getImplDefined2();
    var configuration = MaterialTypeConfiguration.resolveContentType(typeOfRecord, implDefined1[0]);

    var content = masqueradeBlanks(field.getData());
    Map<String, Object> contentMap = new LinkedHashMap<>();
    contentMap.put(TYPE, typeOfRecord);
    contentMap.put(BLVL, change(implDefined1[0]));
    contentMap.put(ELVL, change(implDefined2[0]));
    contentMap.put(DESC, change(implDefined2[1]));
    contentMap.putAll(fillContentMap(MaterialTypeConfiguration.getCommonItems(), content, -1));
    contentMap.putAll(fillContentMap(configuration.getControlFieldItems(),
      content.substring(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX), -1));
    return new FieldItem().tag(field.getTag()).content(contentMap);
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof ControlField
      && field.getTag().equals(GENERAL_INFORMATION_CONTROL_FIELD)
      && marcFormat == MarcFormat.BIBLIOGRAPHIC;
  }

  private String change(char c) {
    return c == SPACE_CHARACTER ? BLANK_REPLACEMENT : Character.toString(c);
  }

}
