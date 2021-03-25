package org.folio.qm.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_OK;

import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.http.Header;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@UtilityClass
public class APITestUtils {

  public static final String TENANT_ID = "test";
  public static final String RECORDS_EDITOR_RECORDS_PATH = "/records-editor/records";
  public static final String RECORDS_EDITOR_RECORDS_STATUS_PATH = "/records-editor/records/status";
  public static final String CHANGE_MANAGER_PARSED_RECORDS_PATH = "/change-manager/parsedRecords";
  public static final String CHANGE_MANAGER_JOB_EXECUTION_PATH = "/change-manager/jobExecutions";
  public static final String CHANGE_MANAGER_JOB_PROFILE_PATH = CHANGE_MANAGER_JOB_EXECUTION_PATH + "/%s/jobProfile";
  public static final String CHANGE_MANAGER_PARSE_RECORDS_PATH = CHANGE_MANAGER_JOB_EXECUTION_PATH + "/%s/records";
  public static final String INSTANCE_ID = "instanceId";
  public static final String QM_RECORD_ID = "qmRecordId";

  public static final String JOHN_TOKEN =
    "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJfaWQiOiIzOGQzYTQ0MS1jMTAwLTVlOGQtYmQxMi03MWJkZTQ5MmI3MjMiLCJpYXQiOjE2MTQyNTIzOTAsInRlbmFudCI6InRlc3QifQ.7IrC11gjVVWSETsC1RfzHvUUcpljYcYJk_TbBf6deBo";
  public static final Header JOHN_TOKEN_HEADER = new Header(TOKEN, JOHN_TOKEN);
  public static final Header JOHN_TOKEN_HEADER_INVALID =
    new Header(TOKEN, "eyJhbGciOiJIUzI1NiJ9.ddd.nBC1esXqYAriVH6J2MfR7QPouzJ8oH5x99CYrU92vi0");

  public static String buildQuery(String parameter, String query) {
    return String.format("?%s=%s", parameter, encodeQuery(query));
  }

  private static String encodeQuery(String query) {
    try {
      return URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new CompletionException(e);
    }
  }

  public static String recordsEditorPath() {
    return RECORDS_EDITOR_RECORDS_PATH;
  }

  public static String recordsEditorPath(String parameter, String value) {
    return recordsEditorPath() + buildQuery(parameter, value);
  }

  public static String recordsEditorStatusPath() {
    return RECORDS_EDITOR_RECORDS_STATUS_PATH;
  }

  public static String recordsEditorStatusPath(String parameter, String value) {
    return recordsEditorStatusPath() + buildQuery(parameter, value);
  }

  public static String recordsEditorResourceByIdPath(String id) {
    return recordsEditorPath() + "/" + id;
  }

  public static String changeManagerPath() {
    return CHANGE_MANAGER_PARSED_RECORDS_PATH;
  }

  public static String changeManagerPath(String parameter, String value) {
    return changeManagerPath() + buildQuery(parameter, value);
  }


  public static String changeManagerResourceByIdPath(String id) {
    return changeManagerPath() + "/" + id;
  }

  public static void mockGet(String url, String body, int status, WireMockServer mockServer) {
    mockServer.stubFor(get(urlEqualTo(url))
      .willReturn(aResponse().withBody(body)
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withStatus(status)));
  }

  public static void mockPost(String url, String filePath, WireMockServer mockServer) {
    mockPost(url, filePath, SC_OK, mockServer);
  }

  public static void mockPost(String url, String filePath, int status, WireMockServer mockServer) {
    mockServer.stubFor(post(urlEqualTo(url))
      .willReturn(aResponse()
        .withStatus(status)
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withBody(IOTestUtils.readFile(filePath))));
  }


  public static void mockPut(String url, int status, WireMockServer mockServer) {
    mockPut(url, null, status, mockServer);
  }

  public static void mockPut(String url, String body, int status, WireMockServer mockServer) {
    var responseDefinitionBuilder = aResponse().withStatus(status);
    if (body != null) {
      responseDefinitionBuilder.withBody(body).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }
    mockServer.stubFor(put(urlEqualTo(url)).willReturn(responseDefinitionBuilder));
  }


}
