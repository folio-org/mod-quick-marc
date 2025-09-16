package org.folio.qm.service;

import org.folio.qm.client.model.DataImportEventPayload;

public interface EventProcessingService {

  void processDataImportCompleted(DataImportEventPayload data);

  void processDataImportError(DataImportEventPayload data);
}
