package org.folio.qm.support.extension.impl;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.utility.DockerImageName.parse;

public class PostgresContainerExtension implements BeforeAllCallback, AfterAllCallback {

  private static final String SPRING_PROPERTY_NAME = "spring.datasource.url";

  private static final DockerImageName DOCKER_IMAGE = parse("postgres:12-alpine");

  private static final String DATABASE_NAME = "folio_test";
  private static final String DATABASE_USERNAME = "folio_admin";
  private static final String DATABASE_PASSWORD = "password";

  private static final PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>(DOCKER_IMAGE)
    .withDatabaseName(DATABASE_NAME)
    .withUsername(DATABASE_USERNAME)
    .withPassword(DATABASE_PASSWORD);

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!CONTAINER.isRunning()) {
      CONTAINER.start();
    }

    System.setProperty(SPRING_PROPERTY_NAME, CONTAINER.getJdbcUrl());
  }

  @Override
  public void afterAll(ExtensionContext context) {
    System.clearProperty(SPRING_PROPERTY_NAME);
  }
}
