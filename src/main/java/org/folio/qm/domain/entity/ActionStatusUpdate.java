package org.folio.qm.domain.entity;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ActionStatusUpdate {

  ActionStatusEnum status;

  String errorMessage;

  UUID externalId;

  UUID marcId;
}
