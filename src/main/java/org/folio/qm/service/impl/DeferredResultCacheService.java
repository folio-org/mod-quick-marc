package org.folio.qm.service.impl;

import static java.util.Objects.requireNonNull;
import static org.folio.qm.config.CacheNames.DATA_IMPORT_RESULT_CACHE;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.FolioExecutionContext;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Log4j2
@Service
@RequiredArgsConstructor
public class DeferredResultCacheService {

  private final CacheManager cacheManager;
  private final FolioExecutionContext context;

  @Cacheable(cacheNames = DATA_IMPORT_RESULT_CACHE, key = "@folioExecutionContext.tenantId + ':' + #jobId")
  public DeferredResult<ResponseEntity<Void>> getDataImportActionResult(UUID jobId) {
    log.info("New DeferredResult was created for [CREATE] action and [{}] jobId", jobId);
    return new DeferredResult<>(60000L);
  }

  public void evictDataImportActionResult(UUID jobId) {
    var key = context.getTenantId() + ":" + jobId;
    log.debug("evictDataImportActionResult:: trying to evict data import action by key: {}", key);
    requireNonNull(cacheManager.getCache(DATA_IMPORT_RESULT_CACHE)).evict(key);
  }
}
