package org.folio.qm.service.impl;

import static org.folio.qm.config.CacheNames.DATA_IMPORT_RESULT_CACHE;
import static org.folio.qm.config.CacheNames.QM_UPDATE_RESULT_CACHE;

import java.util.UUID;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Log4j2
@Service
public class DeferredResultCacheService {

  @Cacheable(cacheNames = QM_UPDATE_RESULT_CACHE, key = "@folioExecutionContext.tenantId + ':' + #recordId")
  public DeferredResult<ResponseEntity<Void>> getUpdateActionResult(UUID recordId) {
    log.info("New DeferredResult was created for [UPDATE] action and [{}] recordId", recordId);
    return new DeferredResult<>();
  }

  @Cacheable(cacheNames = DATA_IMPORT_RESULT_CACHE, key = "@folioExecutionContext.tenantId + ':' + #jobId")
  public DeferredResult<ResponseEntity<Void>> getDataImportActionResult(UUID jobId) {
    log.info("New DeferredResult was created for [DELETE] action and [{}] jobId", jobId);
    return new DeferredResult<>();
  }
}
