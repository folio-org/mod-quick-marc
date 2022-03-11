package org.folio.qm.config;

import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Bean
  public MarcFactory marcFactory(){
    return new MarcFactoryImpl();
  }
}
