package org.folio.qm.service;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

public interface RecordActionService {
  CreationStatus createRecord(QuickMarc quickMarc);

  DeferredResult<ResponseEntity<Void>> deleteRecord(ParsedRecordDto parsedRecordDto);
}
