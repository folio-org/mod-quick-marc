package org.folio.qm.mapper;

import static org.folio.qm.util.MarcUtils.TYPE_MAP;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.SourceRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MarcTypeMapper {

  default ParsedRecordDto.RecordTypeEnum toDto(MarcFormat source) {
    return TYPE_MAP.inverse().get(source);
  }

  default MarcFormat fromDto(ParsedRecordDto.RecordTypeEnum source) {
    return TYPE_MAP.get(source);
  }

  default MarcFormat fromDto(SourceRecord.RecordTypeEnum source) {
    return TYPE_MAP.get(ParsedRecordDto.RecordTypeEnum.fromValue(source.getValue()));
  }
}

