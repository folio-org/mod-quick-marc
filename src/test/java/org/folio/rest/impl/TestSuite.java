package org.folio.rest.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.folio.converter.ContentTypeTest;
import org.folio.converter.Field008RestoreFactoryTest;
import org.folio.converter.Field008SplitterFactoryTest;
import org.folio.converter.QuickMarcToRecordConverterTest;
import org.folio.converter.RecordToQuickMarcConverterTest;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;

import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;

@ExtendWith(VertxExtension.class)
public class TestSuite {

  private static final int okapiPort = NetworkUtils.nextFreePort();
  static final int mockPort = NetworkUtils.nextFreePort();
  private static Vertx vertx;
  private static MockServer mockServer;
  private static boolean isInitialised;

  @BeforeAll
  public static void before() throws InterruptedException, ExecutionException, TimeoutException {
    if (vertx == null) {
      vertx = Vertx.vertx();
    }

    mockServer = new MockServer(mockPort);
    mockServer.start();

    RestAssured.baseURI = "http://localhost:" + okapiPort;
    RestAssured.port = okapiPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    final JsonObject conf = new JsonObject();
    conf.put("http.port", okapiPort);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    CompletableFuture<String> deploymentComplete = new CompletableFuture<>();
    vertx.deployVerticle(RestVerticle.class.getName(), opt, res -> {
      if (res.succeeded()) {
        deploymentComplete.complete(res.result());
      } else {
        deploymentComplete.completeExceptionally(res.cause());
      }
    });
    deploymentComplete.get(60, TimeUnit.SECONDS);
    isInitialised = true;
  }

  @AfterAll
  public static void after() {
    mockServer.close();
    vertx.close();
    isInitialised = false;
  }

  public static boolean isNotInitialised() {
    return !isInitialised;
  }

  @Nested
  class TestGetRecords extends GetRecordsTest {
  }

  @Nested
  class ContentTypeTestsNested extends ContentTypeTest {
  }

  @Nested
  class Field008SplitterFactoryTestsNested extends Field008SplitterFactoryTest {
  }

  @Nested
  class Field008RestoreFactoryTestsNested extends Field008RestoreFactoryTest {
  }

  @Nested
  class RecordToQuickMarcConverterTestNested extends RecordToQuickMarcConverterTest {
  }

  @Nested
  class QuickMarcToRecordConverterTestNested extends QuickMarcToRecordConverterTest {
  }
}
