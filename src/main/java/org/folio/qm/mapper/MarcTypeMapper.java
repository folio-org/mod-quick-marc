package org.folio.qm.mapper;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@Mapper(componentModel = "spring")
public interface MarcTypeMapper {

  BiMap<ParsedRecordDto.RecordType, MarcFormat> typeMap = HashBiMap.create(Map.of(
    ParsedRecordDto.RecordType.MARC_BIB, MarcFormat.BIBLIOGRAPHIC,
    ParsedRecordDto.RecordType.MARC_AUTHORITY, MarcFormat.AUTHORITY,
    ParsedRecordDto.RecordType.MARC_HOLDING, MarcFormat.HOLDINGS
  ));

  default ParsedRecordDto.RecordType toDto(MarcFormat source) {
    return typeMap.inverse().get(source);
  }

  default MarcFormat fromDto(ParsedRecordDto.RecordType source) {
    return typeMap.get(source);
  }
}

