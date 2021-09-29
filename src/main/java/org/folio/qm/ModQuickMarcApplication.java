package org.folio.qm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan("org.folio.qm.config.properties")
public class ModQuickMarcApplication {

  public static void main(String[] args) {
    SpringApplication.run(ModQuickMarcApplication.class, args);
  }

}
