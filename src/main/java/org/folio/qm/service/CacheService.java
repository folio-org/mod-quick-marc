package org.folio.qm.service;

public interface CacheService<T> {

  T getDeleteCache();

  T getUpdateCache();

}
