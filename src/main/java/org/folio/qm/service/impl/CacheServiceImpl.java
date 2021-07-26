package org.folio.qm.service.impl;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import org.folio.qm.service.CacheService;

@Log4j2
@Service
@SuppressWarnings("rawtypes")
public class CacheServiceImpl implements CacheService<DeferredResult> {

  private final Cache<String, DeferredResult> cache;

  public CacheServiceImpl(@Value("${folio.qm.cache.update.max-size:1000}") int maximumSize,
                          @Value("${folio.qm.cache.update.duration.min:10}") int duration) {
    this.cache = CacheBuilder.newBuilder()
      .maximumSize(maximumSize)
      .expireAfterWrite(duration, TimeUnit.MINUTES)
      .build();
  }

  @Override
  public void putToCache(String key, DeferredResult object) {
    log.info("Save DeferredResult to cache with key [{}]", key);
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
