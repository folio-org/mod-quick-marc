package org.folio.qm.converter.impl;

import static org.folio.qm.converter.elements.Constants.HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.impl.MarcHoldingsDtoConverter.HOLDINGS_CONTROL_FIELD_ITEMS;

import java.util.Map;

import org.folio.qm.converter.AbstractMarcQmConverter;
import org.folio.qm.domain.dto.ExternalIdsHolder;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;

public class MarcHoldingsQmConverter extends AbstractMarcQmConverter {

  @Override
  public ParsedRecordDto.RecordTypeEnum supportedType() {
    return ParsedRecordDto.RecordTypeEnum.HOLDING;
  }

  @Override
  protected ExternalIdsHolder constructExternalIdsHolder(QuickMarc quickMarc) {
    return new ExternalIdsHolder()
      .holdingsId(quickMarc.getExternalId())
      .holdingsHrid(quickMarc.getExternalHrid());
  }

  @Override
  protected String restoreGeneralInformationControlField(Map<String, Object> contentMap) {
    return restoreFixedLengthField(HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH, HOLDINGS_CONTROL_FIELD_ITEMS,
      contentMap, 0);
  }
}
