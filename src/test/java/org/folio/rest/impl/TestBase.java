package org.folio.rest.impl;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.impl.TestSuite.mockPort;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class TestBase {

  private static final Logger logger = LoggerFactory.getLogger(TestBase.class);

  private static boolean runningOnOwn;

  protected static final Header X_OKAPI_URL = new Header("X-Okapi-Url", "http://localhost:" + mockPort);
  public static final Header NON_EXIST_CONFIG_X_OKAPI_TENANT = new Header(OKAPI_HEADER_TENANT, "ordersimpltest");

  @BeforeAll
  public static void before() throws InterruptedException, ExecutionException, TimeoutException {
    if (TestSuite.isNotInitialised()) {
      logger.info("Running test on own, initialising suite manually");
      runningOnOwn = true;
      TestSuite.before();
    }
  }

  @AfterAll
  public static void after() {
    if (runningOnOwn) {
      logger.info("Running test on own, un-initialising suite manually");
      TestSuite.after();
    }
  }


  public Response verifyGetRequest(String url, int code) {
    return RestAssured.with()
      .header(X_OKAPI_URL)
      .header(NON_EXIST_CONFIG_X_OKAPI_TENANT)
      .get(url)
      .then()
      .statusCode(code)
      .extract()
      .response();
  }

  public static JsonObject getMockAsJson(String path, String id) {
    return getMockAsJson(String.format("%s%s.json", path, id));
  }

  protected static JsonObject getMockAsJson(String fullPath) {
    try {
      return new JsonObject(getMockData(fullPath));
    } catch (IOException e) {
      fail(e.getMessage());
    }
    return new JsonObject();
  }

  protected static String getMockData(String path) throws IOException {
    logger.info("Using mock datafile: {}", path);
    try (InputStream resourceAsStream = TestBase.class.getClassLoader()
      .getResourceAsStream(path)) {
      if (resourceAsStream != null) {
        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
      } else {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
          lines.forEach(sb::append);
        }
        return sb.toString();
      }
    }
  }

}
