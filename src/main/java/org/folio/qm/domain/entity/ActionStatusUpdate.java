package org.folio.qm.domain.entity;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class ActionStatusUpdate {

  private final ActionStatusEnum status;

  private final String errorMessage;

  private final UUID externalId;

  private final UUID marcId;
}
