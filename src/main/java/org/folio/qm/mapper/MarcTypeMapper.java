package org.folio.qm.mapper;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.mapstruct.Mapper;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;

@Mapper(componentModel = "spring")
public interface MarcTypeMapper {

  BiMap<ParsedRecordDto.RecordTypeEnum, MarcFormat> typeMap = HashBiMap.create(Map.of(
    ParsedRecordDto.RecordTypeEnum.BIB, MarcFormat.BIBLIOGRAPHIC,
    ParsedRecordDto.RecordTypeEnum.AUTHORITY, MarcFormat.AUTHORITY,
    ParsedRecordDto.RecordTypeEnum.HOLDING, MarcFormat.HOLDINGS
  ));

  default ParsedRecordDto.RecordTypeEnum toDto(MarcFormat source) {
    return typeMap.inverse().get(source);
  }

  default MarcFormat fromDto(ParsedRecordDto.RecordTypeEnum source) {
    return typeMap.get(source);
  }
}

