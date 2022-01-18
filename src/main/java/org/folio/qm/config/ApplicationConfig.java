package org.folio.qm.config;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
  /**
   * This bean is used to store tenants which were enabled for this module.
   * It is needed to clear record creation statuses from database for all tenants.
   * */
  @Bean
  public Set<String> tenants() {
    return new ConcurrentSkipListSet<>();
  }
}
