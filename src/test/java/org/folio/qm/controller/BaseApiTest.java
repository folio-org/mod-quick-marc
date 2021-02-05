package org.folio.qm.controller;

import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import wiremock.org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;

import org.folio.qm.extension.DatabaseInitializer;
import org.folio.qm.extension.WireMockInitializer;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.tenant.domain.dto.TenantAttributes;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {WireMockInitializer.class, DatabaseInitializer.class})
class BaseApiTest {

  private static final String TENANT_ID = "test";
  private static boolean dbInitialized = false;

  @Autowired
  protected WireMockServer wireMockServer;
  @Autowired
  protected JdbcTemplate jdbcTemplate;
  @Value("${x-okapi-url}")
  private String okapiUrl;
  @LocalServerPort
  private Integer port;
  @Autowired
  private FolioModuleMetadata metadata;

  @BeforeEach
  void before() {
    if (!dbInitialized) {
      verifyPost("/_/tenant", new TenantAttributes(), HttpStatus.SC_OK);
      dbInitialized = true;
    }
  }

  @AfterEach
  public void afterEach() {
    this.wireMockServer.resetAll();
    JdbcTestUtils.deleteFromTables(jdbcTemplate, creationStatusTable());
  }

  protected Response verifyGet(String path, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .get(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .extract()
      .response();
  }

  protected Response verifyPut(String path, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .put(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  protected Response verifyPost(String path, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .post(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  private String getRequestUrl(String path) {
    return "http://localhost:" + port + path;
  }


  private String creationStatusTable() {
    return metadata.getDBSchemaName(TENANT_ID) + ".record_creation_status";
  }

  protected void saveCreationStatus(UUID id, UUID jobExecutionId) {
    jdbcTemplate.update(
      "INSERT INTO " + creationStatusTable() + " (id, job_execution_id) VALUES (?, ?)", id, jobExecutionId
    );
  }
}
