package org.folio.qm.converter;

import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.impl.MarcAuthorityDtoConverter;
import org.folio.qm.converter.impl.MarcAuthorityQmConverter;
import org.folio.qm.converter.impl.MarcBibliographicDtoConverter;
import org.folio.qm.converter.impl.MarcBibliographicQmConverter;
import org.folio.qm.converter.impl.MarcHoldingsDtoConverter;
import org.folio.qm.converter.impl.MarcHoldingsQmConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@Component
public class MarcConverterFactory {

  public MarcQmConverter findConverter(MarcFormat marcFormat) {
    switch (marcFormat) {
      case BIBLIOGRAPHIC:
        return new MarcBibliographicQmConverter();
      case HOLDINGS:
        return new MarcHoldingsQmConverter();
      case AUTHORITY:
        return new MarcAuthorityQmConverter();
      default:
        throw new IllegalStateException("Unexpected value: " + marcFormat);
    }
  }

  public MarcDtoConverter findConverter(ParsedRecordDto.RecordType recordType, MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc) {
    switch (recordType) {
      case MARC_BIB:
        return new MarcBibliographicDtoConverter(fieldProtectionSettingsMarc);
      case MARC_HOLDING:
        return new MarcHoldingsDtoConverter(fieldProtectionSettingsMarc);
      case MARC_AUTHORITY:
        return new MarcAuthorityDtoConverter(fieldProtectionSettingsMarc);
      default:
        throw new IllegalStateException("Unexpected value: " + recordType);
    }
  }
}
