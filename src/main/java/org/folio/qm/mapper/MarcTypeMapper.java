package org.folio.qm.mapper;

import static org.folio.qm.util.MarcUtils.TYPE_MAP;

import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.domain.dto.MarcFormat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MarcTypeMapper {

  default RecordTypeEnum toDto(MarcFormat source) {
    return TYPE_MAP.inverse().get(source);
  }

  default MarcFormat fromDto(RecordTypeEnum source) {
    return TYPE_MAP.get(source);
  }
}

