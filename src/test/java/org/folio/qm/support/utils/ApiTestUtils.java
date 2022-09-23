package org.folio.qm.support.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.spring.integration.XOkapiHeaders.USER_ID;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import lombok.experimental.UtilityClass;
import org.folio.qm.support.utils.testentities.TestEntitiesUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@UtilityClass
public class ApiTestUtils {

  public static final String TENANT_ID = "test";
  public static final String RECORDS_EDITOR_RECORDS_PATH = "/records-editor/records";
  public static final String RECORDS_EDITOR_RECORDS_STATUS_PATH = "/records-editor/records/status";
  public static final String CHANGE_MANAGER_PARSED_RECORDS_PATH = "/change-manager/parsedRecords";
  public static final String CHANGE_MANAGER_JOB_EXECUTION_PATH = "/change-manager/jobExecutions";
  public static final String CHANGE_MANAGER_JOB_PROFILE_PATH = CHANGE_MANAGER_JOB_EXECUTION_PATH + "/%s/jobProfile";
  public static final String CHANGE_MANAGER_PARSE_RECORDS_PATH = CHANGE_MANAGER_JOB_EXECUTION_PATH + "/%s/records";
  public static final String FIELD_PROTECTION_SETTINGS_PATH = "/field-protection-settings/marc?limit=1000";
  public static final String USERS_PATH = "/users";
  public static final String LINKS_PATH = "/links";
  public static final String LINKS_INSTANCES_PATH = LINKS_PATH + "/instances";

  public static final String EXTERNAL_ID = "externalId";
  public static final String QM_RECORD_ID = "qmRecordId";

  public static final Map<String, String> JOHN_USER_ID_HEADER = Map.of(USER_ID, TestEntitiesUtils.JOHN_USER_ID);

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

  public static String usersByIdPath(String id) {
    return USERS_PATH + "/" + id;
  }

  public static String recordsEditorPath() {
    return RECORDS_EDITOR_RECORDS_PATH;
  }

  public static String recordsEditorPath(String parameter, UUID value) {
    return recordsEditorPath() + buildQuery(parameter, String.valueOf(value));
  }

  public static String recordsEditorStatusPath() {
    return RECORDS_EDITOR_RECORDS_STATUS_PATH;
  }

  public static String recordsEditorStatusPath(String parameter, String value) {
    return recordsEditorStatusPath() + buildQuery(parameter, value);
  }

  public static String recordsEditorResourceByIdPath(UUID id) {
    return recordsEditorPath() + "/" + id;
  }

  public static String changeManagerPath() {
    return CHANGE_MANAGER_PARSED_RECORDS_PATH;
  }

  public static String changeManagerPath(String parameter, UUID value) {
    return changeManagerPath() + buildQuery(parameter, String.valueOf(value));
  }

  public static String changeManagerResourceByIdPath(UUID id) {
    return changeManagerPath() + "/" + id;
  }

  public static String linksByInstanceIdPath(UUID instanceId) {
    return LINKS_INSTANCES_PATH + '/' + instanceId;
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
        .withBody(InputOutputTestUtils.readFile(filePath))));
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
