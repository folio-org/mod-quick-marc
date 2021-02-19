package org.folio.qm.domain.entity;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class RecordCreationStatusUpdate {

  RecordCreationStatusEnum status;

  String errorMessage;

  UUID instanceId;
}
