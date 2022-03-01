package org.folio.qm.service.impl;

import org.folio.qm.util.CacheType;
import org.folio.qm.util.DeferredResultCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.folio.qm.service.CacheService;

@Service
@SuppressWarnings("rawtypes")
public class CacheServiceImpl implements CacheService<DeferredResultCache> {

  private final DeferredResultCache deleteCache;
  private final DeferredResultCache updateCache;

  public CacheServiceImpl(@Value("${folio.qm.cache.update.max-size:1000}") int maximumSize,
                          @Value("${folio.qm.cache.update.duration.min:10}") int duration) {
    this.deleteCache = new DeferredResultCache(maximumSize, duration, CacheType.DELETE);
    this.updateCache = new DeferredResultCache(maximumSize, duration, CacheType.UPDATE);
  }

  @Override
  public DeferredResultCache getDeleteCache() {
    return deleteCache;
  }

  @Override
  public DeferredResultCache getUpdateCache() {
    return updateCache;
  }
}
