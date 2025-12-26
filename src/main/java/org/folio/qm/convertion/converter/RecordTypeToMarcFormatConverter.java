package org.folio.qm.convertion.converter;

import java.util.EnumMap;
import java.util.Map;
import org.folio.Record.RecordType;
import org.folio.qm.domain.dto.MarcFormat;
import org.springframework.stereotype.Component;

@Component
public class RecordTypeToMarcFormatConverter extends AbstractEnumConverter<RecordType, MarcFormat> {

  private static final EnumMap<RecordType, MarcFormat> ENUM_MAP = new EnumMap<>(Map.of(
    RecordType.MARC_BIB, MarcFormat.BIBLIOGRAPHIC,
    RecordType.MARC_AUTHORITY, MarcFormat.AUTHORITY,
    RecordType.MARC_HOLDING, MarcFormat.HOLDINGS
  ));

  public RecordTypeToMarcFormatConverter() {
    super(ENUM_MAP);
  }
}
