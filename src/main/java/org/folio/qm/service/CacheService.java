package org.folio.qm.service;

public interface CacheService<T> {

  void putToCache(String key, T object);

  T getFromCache(String key);

  void invalidate(String key);
}
