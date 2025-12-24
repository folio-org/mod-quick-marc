package org.folio.qm.mapper;

import static org.folio.qm.util.MarcUtils.TYPE_MAP;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.folio.Record.RecordType;
import org.folio.qm.client.model.MappingRecordType;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.domain.dto.MarcFormat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MarcTypeMapper {

  BiMap<MarcFormat, MappingRecordType> MAPPING_MAP = ImmutableBiMap.of(
    MarcFormat.BIBLIOGRAPHIC, MappingRecordType.MARC_BIB,
    MarcFormat.AUTHORITY, MappingRecordType.MARC_AUTHORITY,
    MarcFormat.HOLDINGS, MappingRecordType.MARC_HOLDINGS
  );

  BiMap<MarcFormat, RecordType> SRS_MAP = ImmutableBiMap.of(
    MarcFormat.BIBLIOGRAPHIC, RecordType.MARC_BIB,
    MarcFormat.AUTHORITY, RecordType.MARC_AUTHORITY,
    MarcFormat.HOLDINGS, RecordType.MARC_HOLDING
  );

  default RecordTypeEnum toDto(MarcFormat source) {
    return TYPE_MAP.inverse().get(source);
  }

  default MarcFormat fromDto(RecordTypeEnum source) {
    return TYPE_MAP.get(source);
  }

  default MappingRecordType fromDto(MarcFormat source) {
    return MAPPING_MAP.get(source);
  }

  default RecordType fromDtoToSrsType(MarcFormat marcFormat) {
    return SRS_MAP.get(marcFormat);
  }
}

