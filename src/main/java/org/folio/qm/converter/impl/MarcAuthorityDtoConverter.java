package org.folio.qm.converter.impl;

import static org.folio.qm.converter.elements.Constants.AUTHORITY_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.ControlFieldItem.CAT_RULES;
import static org.folio.qm.converter.elements.ControlFieldItem.DATE_ENTERED;
import static org.folio.qm.converter.elements.ControlFieldItem.GEO_SUBD;
import static org.folio.qm.converter.elements.ControlFieldItem.GOVT_AG;
import static org.folio.qm.converter.elements.ControlFieldItem.KIND_REC;
import static org.folio.qm.converter.elements.ControlFieldItem.LANG_AUTHORITY;
import static org.folio.qm.converter.elements.ControlFieldItem.LEVEL_EST;
import static org.folio.qm.converter.elements.ControlFieldItem.MAIN_USE;
import static org.folio.qm.converter.elements.ControlFieldItem.MOD_REC_EST;
import static org.folio.qm.converter.elements.ControlFieldItem.NUMB_SERIES;
import static org.folio.qm.converter.elements.ControlFieldItem.PERS_NAME;
import static org.folio.qm.converter.elements.ControlFieldItem.REC_UPD;
import static org.folio.qm.converter.elements.ControlFieldItem.REF_EVAL;
import static org.folio.qm.converter.elements.ControlFieldItem.ROMAN;
import static org.folio.qm.converter.elements.ControlFieldItem.SERIES;
import static org.folio.qm.converter.elements.ControlFieldItem.SERIES_USE;
import static org.folio.qm.converter.elements.ControlFieldItem.SH_SYS;
import static org.folio.qm.converter.elements.ControlFieldItem.SOURCE;
import static org.folio.qm.converter.elements.ControlFieldItem.SUBJ_USE;
import static org.folio.qm.converter.elements.ControlFieldItem.TYPE_SUBD;
import static org.folio.qm.converter.elements.ControlFieldItem.UNDEF_18;
import static org.folio.qm.converter.elements.ControlFieldItem.UNDEF_30;
import static org.folio.qm.converter.elements.ControlFieldItem.UNDEF_34;
import static org.folio.qm.util.ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FIELD;
import static org.folio.qm.util.ErrorUtils.buildInternalError;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.folio.qm.converter.AbstractMarcDtoConverter;
import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.exception.ConverterException;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcAuthorityDtoConverter extends AbstractMarcDtoConverter {

  protected static final List<ControlFieldItem> AUTHORITY_CONTROL_FIELD_ITEMS = Arrays.asList(
    DATE_ENTERED,
    GEO_SUBD, ROMAN,
    LANG_AUTHORITY, KIND_REC, CAT_RULES, SH_SYS,
    SERIES, NUMB_SERIES, MAIN_USE, SUBJ_USE,
    SERIES_USE, TYPE_SUBD, UNDEF_18, GOVT_AG,
    REF_EVAL, UNDEF_30, REC_UPD, PERS_NAME,
    LEVEL_EST, UNDEF_34, MOD_REC_EST, SOURCE);

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.AUTHORITY;
  }

  @Override
  protected UUID getExternalId(ParsedRecordDto source) {
    return null;
  }

  @Override
  protected String getExternalHrId(ParsedRecordDto source) {
    return null;
  }

  @Override
  protected Map<String, Object> splitGeneralInformationControlField(String content, String leader) {
    if (content.length() > AUTHORITY_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH) {
      throw new ConverterException(buildInternalError(ILLEGAL_FIXED_LENGTH_CONTROL_FIELD, "Content of 008 field has wrong length"));
    }
    return new LinkedHashMap<>(fillContentMap(AUTHORITY_CONTROL_FIELD_ITEMS, content, 0));
  }
}
