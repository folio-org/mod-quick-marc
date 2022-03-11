package org.folio.qm.controller;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.rest.resource.RecordsEditorAsyncApi;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.impl.DeferredResultCacheService;

@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorAsyncApiImpl implements RecordsEditorAsyncApi {

  private final MarcRecordsService marcRecordsService;
  private final DeferredResultCacheService deferredResultCacheService;

  @Override
  public DeferredResult<ResponseEntity<Void>> putRecord(UUID id, QuickMarc quickMarc) {
    marcRecordsService.updateById(id, quickMarc);
    return deferredResultCacheService.getUpdateActionResult(quickMarc.getParsedRecordDtoId());
  }

  @Override
  public DeferredResult<ResponseEntity<Void>> deleteRecordByExternalId(UUID id) {
    var status = marcRecordsService.deleteByExternalId(id);
    return deferredResultCacheService.getDataImportActionResult(status.getJobExecutionId());
  }
}
