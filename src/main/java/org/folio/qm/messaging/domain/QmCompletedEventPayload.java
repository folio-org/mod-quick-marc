package org.folio.qm.messaging.domain;

import lombok.Data;

@Data
public class QmCompletedEventPayload {

  private String recordId;
  private boolean isSucceed;
  private String errorMessage;
}
