package org.folio.qm.converter.impl;

import static org.folio.qm.converter.elements.Constants.AUTHORITY_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH;
import static org.folio.qm.converter.impl.MarcAuthorityDtoConverter.AUTHORITY_CONTROL_FIELD_ITEMS;

import java.util.Map;

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
    return null;
  }

  @Override
  protected String restoreGeneralInformationControlField(Map<String, Object> contentMap) {
    return restoreFixedLengthField(AUTHORITY_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH, AUTHORITY_CONTROL_FIELD_ITEMS,
      contentMap, 0);
  }
}
