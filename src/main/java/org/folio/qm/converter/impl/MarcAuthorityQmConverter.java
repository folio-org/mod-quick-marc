package org.folio.qm.converter.impl;

import org.folio.qm.converter.AbstractMarcQmConverter;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcAuthorityQmConverter extends AbstractMarcQmConverter {

  @Override
  public ParsedRecordDto.RecordType supportedType() {
    return ParsedRecordDto.RecordType.MARC_AUTHORITY;
  }

  @Override
  protected ExternalIdsHolder constructExternalIdsHolder(QuickMarc quickMarc) {
    return new ExternalIdsHolder()
      .withAuthorityId(String.valueOf(quickMarc.getExternalId()))
      .withAuthorityHrid(quickMarc.getExternalHrid());
  }

}
