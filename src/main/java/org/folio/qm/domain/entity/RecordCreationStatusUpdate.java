package org.folio.qm.domain.entity;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecordCreationStatusUpdate {

  RecordCreationStatusEnum status;

  String errorMessage;

  UUID externalId;

  UUID marcId;
}
