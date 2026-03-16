package org.folio.qm.convertion.converter;

import java.util.EnumMap;
import java.util.Map;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.MappingRecordType;
import org.springframework.stereotype.Component;

@Component
public class MarcFormatToMappingRecordTypeConverter extends AbstractEnumConverter<MarcFormat, MappingRecordType> {

  private static final EnumMap<MarcFormat, MappingRecordType> ENUM_MAP = new EnumMap<>(Map.of(
    MarcFormat.BIBLIOGRAPHIC, MappingRecordType.MARC_BIB,
    MarcFormat.AUTHORITY, MappingRecordType.MARC_AUTHORITY,
    MarcFormat.HOLDINGS, MappingRecordType.MARC_HOLDINGS
  ));

  public MarcFormatToMappingRecordTypeConverter() {
    super(ENUM_MAP);
  }
}
