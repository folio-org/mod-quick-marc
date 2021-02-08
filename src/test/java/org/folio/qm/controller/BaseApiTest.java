package org.folio.qm.controller;

import static org.folio.qm.utils.TestUtils.TENANT_ID;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import wiremock.org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import org.folio.qm.extension.DatabaseExtension;
import org.folio.qm.extension.WireMockInitializer;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.tenant.domain.dto.TenantAttributes;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@ExtendWith(DatabaseExtension.class)
@AutoConfigureEmbeddedDatabase
class BaseApiTest {

  private static boolean dbInitialized = false;

  @Autowired
  protected WireMockServer wireMockServer;
  @Autowired
  protected FolioModuleMetadata metadata;
  @Autowired
  protected JdbcTemplate jdbcTemplate;
  @Value("${x-okapi-url}")
  private String okapiUrl;
  @LocalServerPort
  private Integer port;

  @BeforeEach
  void before() {
    if (!dbInitialized) {
      verifyPost("/_/tenant", new TenantAttributes(), HttpStatus.SC_OK);
      dbInitialized = true;
    }
  }

  @AfterEach
  void afterEach() {
    this.wireMockServer.resetAll();
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

}
