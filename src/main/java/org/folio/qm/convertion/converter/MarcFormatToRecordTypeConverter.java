package org.folio.qm.convertion.converter;

import java.util.EnumMap;
import java.util.Map;
import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.MarcFormat;
import org.springframework.stereotype.Component;

@Component
public class MarcFormatToRecordTypeConverter extends AbstractEnumConverter<MarcFormat, RecordType> {

  private static final EnumMap<MarcFormat, RecordType> ENUM_MAP = new EnumMap<>(Map.of(
    MarcFormat.BIBLIOGRAPHIC, RecordType.MARC_BIB,
    MarcFormat.AUTHORITY, RecordType.MARC_AUTHORITY,
    MarcFormat.HOLDINGS, RecordType.MARC_HOLDING
  ));

  public MarcFormatToRecordTypeConverter() {
    super(ENUM_MAP);
  }
}
