package org.folio.qm.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import org.folio.qm.service.CacheService;

@Service
@SuppressWarnings("rawtypes")
public class CacheServiceImpl implements CacheService<DeferredResult> {

  private final Cache<String, DeferredResult> cache = CacheBuilder.newBuilder().maximumSize(1000).build();

  @Override
  public void putToCache(String key, DeferredResult object) {
    cache.put(key, object);
  }

  @Override
  public DeferredResult getFromCache(String key) {
    return cache.getIfPresent(key);
  }

  @Override
  public void invalidate(String key) {
    cache.invalidate(key);
  }
}
