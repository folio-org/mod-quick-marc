package org.folio.qm.converter.impl;

import org.folio.qm.converter.AbstractMarcQmConverter;
import org.folio.qm.domain.dto.ExternalIdsHolder;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;

public class MarcAuthorityQmConverter extends AbstractMarcQmConverter {

  @Override
  public ParsedRecordDto.RecordTypeEnum supportedType() {
    return ParsedRecordDto.RecordTypeEnum.AUTHORITY;
  }

  @Override
  protected ExternalIdsHolder constructExternalIdsHolder(QuickMarc quickMarc) {
    return new ExternalIdsHolder()
      .authorityId(quickMarc.getExternalId())
      .authorityHrid(quickMarc.getExternalHrid());
  }

}
