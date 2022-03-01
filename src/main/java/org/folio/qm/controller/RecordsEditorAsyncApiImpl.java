package org.folio.qm.controller;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.folio.qm.util.DeferredResultCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.rest.resource.RecordsEditorAsyncApi;
import org.folio.qm.service.CacheService;
import org.folio.qm.service.MarcRecordsService;

@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorAsyncApiImpl implements RecordsEditorAsyncApi {

  private final MarcRecordsService marcRecordsService;
  private final CacheService<DeferredResultCache> cacheService;

  @Override
  public DeferredResult<ResponseEntity<Void>> deleteRecordByExternalId(UUID id) {
    return marcRecordsService.deleteByExternalId(id);
  }

  @Override
  public DeferredResult<ResponseEntity<Void>> putRecord(UUID id, QuickMarc quickMarc) {
    var deferredResult = new DeferredResult<ResponseEntity<Void>>();

    cacheService.getUpdateCache().putToCache(String.valueOf(quickMarc.getParsedRecordDtoId()), deferredResult);
    marcRecordsService.updateById(id, quickMarc);
    return deferredResult;
  }
}
