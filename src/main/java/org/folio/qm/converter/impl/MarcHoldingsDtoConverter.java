package org.folio.qm.converter.impl;

import static org.folio.qm.converter.elements.Constants.HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.elements.ControlFieldItem.ACQ_ENDDATE;
import static org.folio.qm.converter.elements.ControlFieldItem.ACQ_METHOD;
import static org.folio.qm.converter.elements.ControlFieldItem.ACQ_STATUS;
import static org.folio.qm.converter.elements.ControlFieldItem.COMPL;
import static org.folio.qm.converter.elements.ControlFieldItem.COPIES;
import static org.folio.qm.converter.elements.ControlFieldItem.DATE_ENTERED;
import static org.folio.qm.converter.elements.ControlFieldItem.GEN_RET;
import static org.folio.qm.converter.elements.ControlFieldItem.LANG_HOLDINGS;
import static org.folio.qm.converter.elements.ControlFieldItem.LEND;
import static org.folio.qm.converter.elements.ControlFieldItem.REPRO;
import static org.folio.qm.converter.elements.ControlFieldItem.REPT_DATE;
import static org.folio.qm.converter.elements.ControlFieldItem.SEP_COMP;
import static org.folio.qm.converter.elements.ControlFieldItem.SPEC_RET;
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

public class MarcHoldingsDtoConverter extends AbstractMarcDtoConverter {

  protected static final List<ControlFieldItem> HOLDINGS_CONTROL_FIELD_ITEMS = Arrays.asList(ACQ_STATUS, ACQ_METHOD,
      ACQ_ENDDATE, COMPL, COPIES,
      DATE_ENTERED, GEN_RET, LANG_HOLDINGS, LEND, REPRO,
      REPT_DATE, SEP_COMP, SPEC_RET);

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.HOLDINGS;
  }

  @Override
  protected UUID getExternalId(ParsedRecordDto source) {
    return UUID.fromString(source.getExternalIdsHolder().getHoldingsId());
  }

  @Override
  protected String getExternalHrId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getHoldingsHrid();
  }

  protected Map<String, Object> splitGeneralInformationControlField(String content, String leader) {
    if(content.length() > HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH) {
      throw new ConverterException(buildInternalError(ILLEGAL_FIXED_LENGTH_CONTROL_FIELD, "Content of 008 field has wrong length"));
    }
    return new LinkedHashMap<>(fillContentMap(HOLDINGS_CONTROL_FIELD_ITEMS, content, 0));
  }
}
