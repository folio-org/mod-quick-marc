package org.folio.qm.domain.entity;

import java.util.UUID;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
@ToString
public class RecordCreationStatusUpdate {

  RecordCreationStatusEnum status;

  String errorMessage;

  UUID instanceId;
}
