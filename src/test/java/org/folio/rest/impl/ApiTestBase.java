package org.folio.rest.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.TestSuite.isInitialized;
import static org.folio.TestSuite.mockPort;
import static org.folio.TestSuite.wireMockServer;
import static org.folio.rest.RestVerticle.*;
import static org.folio.util.Constants.OKAPI_URL;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.folio.TestSuite;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;

public class ApiTestBase {

  public static final String PARSED_RECORD_DTO_PATH = "mockdata/change-manager/parsedRecordDto.json";
  public static final String QUICK_MARC_RECORD_PATH = "mockdata/quick-marc-json/quickMarcJson.json";
  public static final String QUICK_MARC_LEADER_MISMATCH = "mockdata/quick-marc-json/quickMarcJsonLeaderMismatch.json";
  public static final String QUICK_MARC_WRONG_ITEM_LENGTH = "mockdata/quick-marc-json/quickMarcJsonWrongItemLength.json";
  private static final Header X_OKAPI_URL = new Header(OKAPI_URL, "http://localhost:" + mockPort);
  private static final Header X_OKAPI_TENANT = new Header(OKAPI_HEADER_TENANT, "quickmarctest");

  @BeforeAll
  public static void globalSetUp() throws InterruptedException, ExecutionException, TimeoutException {
    if (!isInitialized) {
      TestSuite.globalSetUp();
    }
  }

  @BeforeEach
  public void setUp() {
    wireMockServer.resetAll();
  }

  @AfterAll
  public static void globalTearDown() {
    if (isInitialized) {
      TestSuite.globalTearDown();
    }
  }

  public Response verifyGetRequest(String url, int code) {
    return RestAssured
      .with()
        .header(X_OKAPI_URL)
        .header(X_OKAPI_TENANT)
      .get(url)
        .then()
          .statusCode(code)
          .extract()
            .response();
  }

  public Response verifyPutRequest(String url, Object requestBody, int code) {
    return RestAssured
      .with()
        .header(X_OKAPI_URL)
        .header(X_OKAPI_TENANT)
        .body(requestBody)
        .contentType(APPLICATION_JSON)
      .put(url)
        .then()
          .statusCode(code)
          .extract()
            .response();
  }

  JsonObject getJsonObject(String path) {
    try {
      return new JsonObject(getMockData(path));
    } catch (Exception e) {
      return new JsonObject();
    }
  }

  private String getMockData(String path) throws IOException {
    try (InputStream stream = QuickMarcApiTest.class.getClassLoader()
      .getResourceAsStream(path)) {
      if (!Objects.isNull(stream)) {
        return IOUtils.toString(stream, StandardCharsets.UTF_8);
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
