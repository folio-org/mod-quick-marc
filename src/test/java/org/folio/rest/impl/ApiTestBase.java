package org.folio.rest.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.TestSuite.isInitialized;
import static org.folio.TestSuite.mockPort;
import static org.folio.TestSuite.wireMockServer;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.util.Constants.OKAPI_URL;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.folio.TestSuite;
import org.folio.srs.model.Record;
import org.folio.srs.model.RecordCollection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;

public class ApiTestBase {

  public static final String SRS_RECORDS_COLLECTION_PATH = "mockdata/srs-records/records.json";
  public static final String QUICK_MARC_RECORD_PATH = "mockdata/quick-marc-json/quickMarcJson.json";
  public static final String INVALID_QUICK_MARC_RECORD_PATH = "mockdata/quick-marc-json/quickMarcJsonWrong008Items.json";
  public static final String UPDATED_RECORD_PATH = "mockdata/srs-records/recordForPut.json";
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

  public Response verifyPut(String url, Object requestBody, int code) {
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

  JsonObject getSrsRecordsBySearchParameter(String instanceId) {
    List<Record> records;
    try {
      records = new JsonObject(getMockData(SRS_RECORDS_COLLECTION_PATH)).mapTo(RecordCollection.class).getRecords();
    } catch (IOException e) {
      records = new ArrayList<>();
    }
    records.removeIf(record -> !Objects.equals(record.getExternalIdsHolder().getInstanceId(), instanceId));
    RecordCollection collection = new RecordCollection().withRecords(records).withTotalRecords(records.size());
    return JsonObject.mapFrom(collection);
  }

  JsonObject getJsonObject(String path) {
    try {
      JsonObject jsonObject = new JsonObject(getMockData(path));
      return jsonObject;
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
