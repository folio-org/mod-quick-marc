package org.folio.qm.converter.impl;

import java.util.UUID;

import org.folio.qm.converter.AbstractMarcDtoConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcBibliographicDtoConverter extends AbstractMarcDtoConverter {

  public MarcBibliographicDtoConverter(MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc) {
    super(fieldProtectionSettingsMarc);
  }

  @Override
  protected UUID getExternalId(ParsedRecordDto source) {
    return UUID.fromString(source.getExternalIdsHolder().getInstanceId());
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
