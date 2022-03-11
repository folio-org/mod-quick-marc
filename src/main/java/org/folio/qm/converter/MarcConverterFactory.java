package org.folio.qm.converter;

import org.springframework.stereotype.Component;

import org.folio.qm.converter.impl.MarcAuthorityDtoConverter;
import org.folio.qm.converter.impl.MarcAuthorityQmConverter;
import org.folio.qm.converter.impl.MarcBibliographicDtoConverter;
import org.folio.qm.converter.impl.MarcBibliographicQmConverter;
import org.folio.qm.converter.impl.MarcHoldingsDtoConverter;
import org.folio.qm.converter.impl.MarcHoldingsQmConverter;
import org.folio.qm.domain.dto.MarcFieldProtectionSettingsCollection;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;

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

  public MarcDtoConverter findConverter(ParsedRecordDto.RecordTypeEnum recordType,
                                        MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc) {
    switch (recordType) {
      case BIB:
        return new MarcBibliographicDtoConverter(fieldProtectionSettingsMarc);
      case HOLDING:
        return new MarcHoldingsDtoConverter(fieldProtectionSettingsMarc);
      case AUTHORITY:
        return new MarcAuthorityDtoConverter(fieldProtectionSettingsMarc);
      default:
        throw new IllegalStateException("Unexpected value: " + recordType);
    }
  }
}
