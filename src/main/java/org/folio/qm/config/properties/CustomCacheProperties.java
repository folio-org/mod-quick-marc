package org.folio.qm.config.properties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "folio.cache")
public class CustomCacheProperties {

  private final Map<String, CustomCache> spec = new HashMap<>();

  public record CustomCache(long maximumSize, Duration ttl) { }
}
