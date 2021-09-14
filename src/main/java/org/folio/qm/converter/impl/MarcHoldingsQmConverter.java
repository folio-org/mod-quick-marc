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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.folio.qm.converter.AbstractMarcQmConverter;
import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcHoldingsQmConverter extends AbstractMarcQmConverter {

  @Override
  public ParsedRecordDto.RecordType supportedType() {
    return ParsedRecordDto.RecordType.MARC_HOLDING;
  }

  @Override
  protected ExternalIdsHolder constructExternalIdsHolder(QuickMarc quickMarc) {
    return new ExternalIdsHolder()
      .withHoldingsId(quickMarc.getExternalId())
      .withHoldingsHrid(quickMarc.getExternalHrid());
  }

  @Override
  protected String restoreGeneralInformationControlField(Map<String, Object> contentMap) {
    List<ControlFieldItem> controlFieldItems = Arrays.asList(ACQ_STATUS, ACQ_METHOD, ACQ_ENDDATE, COMPL, COPIES,
                                                             DATE_ENTERED, GEN_RET, LANG_HOLDINGS, LEND, REPRO, REPT_DATE,
                                                             SEP_COMP, SPEC_RET);
      return restoreFixedLengthField(HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH, controlFieldItems,
          contentMap, 0);
    }
}
