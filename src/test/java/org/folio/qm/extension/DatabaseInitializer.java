package org.folio.qm.extension;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class DatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:12-alpine").waitingFor(Wait.forListeningPort());
    container.start();

    configurableApplicationContext.addApplicationListener(applicationEvent -> {
      if (applicationEvent instanceof ContextClosedEvent) {
        container.stop();
      }
    });

    TestPropertyValues
      .of(
        "spring.datasource.username:" + container.getUsername(),
        "spring.datasource.password:" + container.getPassword(),
        "spring.datasource.url:" + container.getJdbcUrl(),
        "spring.datasource.driver-class-name:" + container.getDriverClassName()
      )
      .applyTo(configurableApplicationContext);
  }
}
