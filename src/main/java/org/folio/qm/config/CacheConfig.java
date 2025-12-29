package org.folio.qm.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import org.folio.qm.config.properties.CustomCacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
@EnableConfigurationProperties({CacheProperties.class, CustomCacheProperties.class})
public class CacheConfig {

  public static final String QM_FETCH_LINKING_RULES_RESULTS = "linking-rules-results";
  public static final String SPECIFICATION_STORAGE_CACHE = "specifications";

  @Bean
  public CacheManager cacheManager(CacheProperties cacheProperties, CustomCacheProperties customCacheProperties) {
    Collection<Cache> caches = new ArrayList<>();
    for (String cacheName : cacheProperties.getCacheNames()) {
      var customCacheSpec = customCacheProperties.getSpec().get(cacheName);
      if (customCacheSpec == null) {
        caches.add(buildDefaultCache(cacheName, cacheProperties.getCaffeine().getSpec()));
      } else {
        caches.add(buildCustomCache(cacheName, customCacheSpec.ttl(), customCacheSpec.maximumSize()));
      }
    }
    var manager = new SimpleCacheManager();
    manager.setCaches(caches);
    return manager;
  }

  private CaffeineCache buildDefaultCache(String name, String spec) {
    return new CaffeineCache(name, Caffeine.from(spec).build());
  }

  private CaffeineCache buildCustomCache(String name, Duration durationToExpire, long maximumSize) {
    return new CaffeineCache(name, Caffeine.newBuilder()
      .expireAfterWrite(durationToExpire)
      .maximumSize(maximumSize)
      .build());
  }
}
