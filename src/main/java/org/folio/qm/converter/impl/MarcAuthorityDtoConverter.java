package org.folio.qm.converter.impl;

import java.util.UUID;

import org.folio.qm.converter.AbstractMarcDtoConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcAuthorityDtoConverter extends AbstractMarcDtoConverter {

  public MarcAuthorityDtoConverter(MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc) {
    super(fieldProtectionSettingsMarc);
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.AUTHORITY;
  }

  @Override
  protected UUID getExternalId(ParsedRecordDto source) {
    return source.getExternalIdsHolder() != null ? UUID.fromString(source.getExternalIdsHolder().getAuthorityId()) : null;
  }

  @Override
  protected String getExternalHrId(ParsedRecordDto source) {
    return source.getExternalIdsHolder() != null ? source.getExternalIdsHolder().getAuthorityHrid() : null;
  }

}
