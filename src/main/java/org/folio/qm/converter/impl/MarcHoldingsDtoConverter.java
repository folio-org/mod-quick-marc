package org.folio.qm.converter.impl;

import org.folio.qm.converter.AbstractMarcDtoConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcHoldingsDtoConverter extends AbstractMarcDtoConverter {

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.HOLDINGS;
  }

  @Override
  protected String getExternalId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getHoldingsId();
  }

  @Override
  protected String getExternalHrId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getHoldingsHrid();
  }
}
