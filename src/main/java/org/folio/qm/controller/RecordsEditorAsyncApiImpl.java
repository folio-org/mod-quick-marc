package org.folio.qm.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.rest.resource.RecordsEditorAsyncApi;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.impl.DeferredResultCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@Validated
@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorAsyncApiImpl implements RecordsEditorAsyncApi {

  private final MarcRecordsService marcRecordsService;
  private final DeferredResultCacheService deferredResultCacheService;

  @Override
  public DeferredResult<ResponseEntity<Void>> putRecord(UUID id, QuickMarcEdit quickMarc) {
    var updateActionResult = deferredResultCacheService.getUpdateActionResult(id);
    marcRecordsService.updateById(id, quickMarc, updateActionResult);
    return updateActionResult;
  }
}
