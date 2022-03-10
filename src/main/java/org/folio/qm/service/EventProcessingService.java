package org.folio.qm.service;

import org.folio.qm.domain.dto.DataImportEventPayload;

public interface EventProcessingService {

  void processDICompleted(DataImportEventPayload data);

  void processDIError(DataImportEventPayload data);
}
