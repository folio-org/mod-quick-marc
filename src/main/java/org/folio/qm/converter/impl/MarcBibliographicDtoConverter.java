package org.folio.qm.converter.impl;

import org.folio.qm.converter.AbstractMarcDtoConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcBibliographicDtoConverter extends AbstractMarcDtoConverter {

  @Override
  protected String getExternalId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getInstanceId();
  }

  @Override
  protected String getExternalHrId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getInstanceHrid();
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.BIBLIOGRAPHIC;
  }
}
