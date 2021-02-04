package org.folio.qm.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.folio.qm.extension.WireMockInitializer;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import wiremock.org.apache.commons.lang3.StringUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {WireMockInitializer.class})
class BaseApiTest {

  @Autowired
  protected WireMockServer wireMockServer;
  @Value("${x-okapi-url}")
  private String okapiUrl;
  @LocalServerPort
  private Integer port;

  @AfterEach
  public void afterEach() {
    this.wireMockServer.resetAll();
  }

  protected Response verifyGet(String url, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, "test"))
      .get("http://localhost:" + port + url)
      .then()
      .statusCode(code)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .extract()
      .response();
  }

  protected Response verifyPost(String url, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, "test"))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .post("http://localhost:" + port + url)
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  protected Response verifyPut(String url, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, "test"))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .put("http://localhost:" + port + url)
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }
}
