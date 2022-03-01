package org.folio.qm.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.TimeUnit;

@Log4j2
public class DeferredResultCache {
  private final Cache<String, DeferredResult> cache;
  private final CacheType cacheType;

  public DeferredResultCache(int maximumSize, int duration, CacheType cacheType) {
    this.cache = CacheBuilder.newBuilder()
      .maximumSize(maximumSize)
      .expireAfterWrite(duration, TimeUnit.MINUTES)
      .build();
    this.cacheType = cacheType;
  }

  public void putToCache(String key, DeferredResult object) {
    log.info("Save DeferredResult to {} cache with key [{}]", key, cacheType);
    cache.put(key, object);
  }

  public DeferredResult getFromCache(String key) {
    return cache.getIfPresent(key);
  }

  public void invalidate(String key) {
    cache.invalidate(key);
  }

}
