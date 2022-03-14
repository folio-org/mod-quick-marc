package org.folio.qm.mapper;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatus;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CreationStatusMapper {

  @Mappings({
    @Mapping(target = "qmRecordId", source = "id"),
    @Mapping(target = "status",
             expression = "java(CreationStatus.StatusEnum.fromValue(recordCreationStatus.getStatus().toString()))"),
    @Mapping(target = "metadata.createdAt", source = "createdAt"),
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
  })
  CreationStatus fromEntity(RecordCreationStatus recordCreationStatus);

  default OffsetDateTime map(Timestamp value) {
    return value != null ? OffsetDateTime.from(value.toInstant().atZone(ZoneId.systemDefault())) : null;
  }
}

