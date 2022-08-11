package org.folio.qm.service;

import org.folio.qm.domain.dto.DataImportEventPayload;

public interface EventProcessingService {

  void processDataImportCompleted(DataImportEventPayload data);

  void processDataImportError(DataImportEventPayload data);
}
