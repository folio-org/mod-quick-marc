package org.folio.qm.service;

import org.folio.rest.jaxrs.model.DataImportEventPayload;

public interface DataImportEventProcessingService {

  void processDICompleted(DataImportEventPayload data);

  void processDIError(DataImportEventPayload data);
}
