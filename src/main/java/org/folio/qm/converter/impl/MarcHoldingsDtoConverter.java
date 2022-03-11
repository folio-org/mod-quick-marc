package org.folio.qm.converter.impl;

import java.util.UUID;

import org.folio.qm.converter.AbstractMarcDtoConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcHoldingsDtoConverter extends AbstractMarcDtoConverter {

  public MarcHoldingsDtoConverter(MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc) {
    super(fieldProtectionSettingsMarc);
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.HOLDINGS;
  }

  @Override
  protected UUID getExternalId(ParsedRecordDto source) {
    return UUID.fromString(source.getExternalIdsHolder().getHoldingsId());
  }

  @Override
  protected String getExternalHrId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getHoldingsHrid();
  }

}
