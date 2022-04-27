package org.folio.qm.mapper;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.RecordActionStatus;
import org.folio.qm.domain.entity.ActionStatus;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ActionStatusMapper {

  @Mapping(target = "actionId", source = "id")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "metadata.createdAt", source = "createdAt")
  @Mapping(target = "metadata.updatedAt", source = "updatedAt")
  @Mapping(target = "actionType", source = "jobProfile.profileAction")
  @Mapping(target = "marcFormat", source = "jobProfile.recordType")
  RecordActionStatus fromEntity(ActionStatus actionStatus);

  default OffsetDateTime map(Timestamp value) {
    return value != null ? OffsetDateTime.from(value.toInstant().atZone(ZoneOffset.UTC)) : null;
  }

  default MarcFormat map(RecordType recordType) {
    return MarcFormat.fromValue(recordType.name().replace("MARC_", ""));
  }

  default RecordActionStatus.ActionTypeEnum map(JobProfileAction action) {
    return RecordActionStatus.ActionTypeEnum.fromValue(action.name());
  }

  default RecordActionStatus.StatusEnum map(ActionStatusEnum actionStatus) {
    return RecordActionStatus.StatusEnum.fromValue(actionStatus.name());
  }
}

