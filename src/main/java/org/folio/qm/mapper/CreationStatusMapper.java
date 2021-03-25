package org.folio.qm.mapper;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatus;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CreationStatusMapper {

  @Mappings({
    @Mapping(target = "qmRecordId", expression = "java(uuidToStringSafe(recordCreationStatus.getId()))"),
    @Mapping(target = "instanceId", expression = "java(uuidToStringSafe(recordCreationStatus.getInstanceId()))"),
    @Mapping(target = "jobExecutionId", expression = "java(uuidToStringSafe(recordCreationStatus.getJobExecutionId()))"),
    @Mapping(target = "status", expression = "java(CreationStatus.StatusEnum.fromValue(recordCreationStatus.getStatus().toString()))"),
    @Mapping(target = "metadata.createdAt", source = "createdAt"),
    @Mapping(target = "metadata.updatedAt", source = "updatedAt")
  })
  CreationStatus fromEntity(RecordCreationStatus recordCreationStatus);

  default String uuidToStringSafe(UUID uuid) {
    return uuid != null ? uuid.toString() : null;
  }

  default OffsetDateTime map(Timestamp value) {
    return value != null ? OffsetDateTime.from(value.toInstant().atZone(ZoneId.systemDefault())) : null;
  }
}

