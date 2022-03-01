package org.folio.qm.service;

import org.folio.rest.jaxrs.model.DataImportEventPayload;

public interface EventProcessingService {

  boolean processDICompleted(DataImportEventPayload data);

  boolean processDIError(DataImportEventPayload data);
}
