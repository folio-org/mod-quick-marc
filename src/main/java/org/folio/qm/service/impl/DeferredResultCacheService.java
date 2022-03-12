package org.folio.qm.service.impl;

import static org.folio.qm.config.CacheNames.DATA_IMPORT_RESULT_CACHE;
import static org.folio.qm.config.CacheNames.QM_UPDATE_RESULT_CACHE;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class DeferredResultCacheService {

  @Cacheable(cacheNames = QM_UPDATE_RESULT_CACHE, key = "@folioExecutionContext.tenantId + ':' + #recordId")
  public DeferredResult<ResponseEntity<Void>> getUpdateActionResult(UUID recordId) {
    return new DeferredResult<>();
  }

  @Cacheable(cacheNames = DATA_IMPORT_RESULT_CACHE, key = "@folioExecutionContext.tenantId + ':' + #jobId")
  public DeferredResult<ResponseEntity<Void>> getDataImportActionResult(UUID jobId) {
    return new DeferredResult<>();
  }
}
