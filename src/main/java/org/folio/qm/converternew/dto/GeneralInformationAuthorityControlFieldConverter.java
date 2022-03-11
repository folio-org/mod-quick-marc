package org.folio.qm.converternew.dto;

import static org.folio.qm.converter.elements.Constants.AUTHORITY_CONTROL_FIELD_ITEMS;
import static org.folio.qm.converter.elements.Constants.AUTHORITY_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.Constants.BLVL;
import static org.folio.qm.converter.elements.Constants.DESC;
import static org.folio.qm.converter.elements.Constants.ELVL;
import static org.folio.qm.converter.elements.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.qm.converter.elements.Constants.TYPE;
import static org.folio.qm.util.ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FIELD;
import static org.folio.qm.util.ErrorUtils.buildInternalError;

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
import org.folio.qm.exception.ConverterException;

@Component
public class GeneralInformationAuthorityControlFieldConverter implements VariableFieldConverter<ControlField> {

  @Override
  public FieldItem convert(ControlField field, Leader leader) {
    var content = masqueradeBlanks(field.getData());
    if (content.length() > AUTHORITY_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH) {
      throw new ConverterException(buildInternalError(ILLEGAL_FIXED_LENGTH_CONTROL_FIELD, "Content of 008 field has wrong length"));
    }
    var contentMap = new LinkedHashMap<>(fillContentMap(AUTHORITY_CONTROL_FIELD_ITEMS, content, 0));
    return new FieldItem().tag(field.getTag()).content(contentMap);
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof ControlField
      && field.getTag().equals(GENERAL_INFORMATION_CONTROL_FIELD)
      && marcFormat == MarcFormat.AUTHORITY;
  }

}
