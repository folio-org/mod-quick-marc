package org.folio.qm.converter.impl;

import static org.folio.qm.converter.elements.Constants.HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.impl.MarcHoldingsDtoConverter.HOLDINGS_CONTROL_FIELD_ITEMS;

import java.util.Map;

import org.folio.qm.converter.AbstractMarcQmConverter;
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
      .withHoldingsId(String.valueOf(quickMarc.getExternalId()))
      .withHoldingsHrid(quickMarc.getExternalHrid());
  }

  @Override
  protected String restoreGeneralInformationControlField(Map<String, Object> contentMap) {
      return restoreFixedLengthField(HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH, HOLDINGS_CONTROL_FIELD_ITEMS,
          contentMap, 0);
    }
}
