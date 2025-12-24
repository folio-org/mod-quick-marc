package org.folio.support.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.spring.integration.XOkapiHeaders.USER_ID;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@UtilityClass
public class ApiTestUtils {

  public static final String TENANT_ID = "test";
  public static final String RECORDS_EDITOR_RECORDS_PATH = "/records-editor/records";
  public static final String RECORDS_EDITOR_VALIDATE_PATH = "/records-editor/validate";
  public static final String SOURCE_STORAGE_RECORDS_PATH = "/source-storage/records";
  public static final String USERS_PATH = "/users";
  public static final String LINKS_PATH = "/links";
  public static final String LINKS_INSTANCES_PATH = LINKS_PATH + "/instances";
  public static final String MARC_SPECIFICATIONS_PATH = "/marc-specifications";

  public static final String EXTERNAL_ID = "externalId";

  public static final Map<String, String> JOHN_USER_ID_HEADER = Map.of(USER_ID, TestEntitiesUtils.JOHN_USER_ID);

  public static String buildQuery(String parameter, String query) {
    return String.format("?%s=%s", parameter, encodeQuery(query));
  }

  public static String usersByIdPath(String id) {
    return USERS_PATH + "/" + id;
  }

  public static String recordsEditorPath() {
    return RECORDS_EDITOR_RECORDS_PATH;
  }

  public static String recordsEditorPath(String parameter, String value) {
    return recordsEditorPath() + buildQuery(parameter, String.valueOf(value));
  }

  public static String recordsEditorPath(String externalId) {
    return recordsEditorPath() + buildQuery(EXTERNAL_ID, externalId);
  }

  public static String recordsEditorValidatePath() {
    return RECORDS_EDITOR_VALIDATE_PATH;
  }

  public static String marcSpecificationsPath() {
    return MARC_SPECIFICATIONS_PATH;
  }

  public static String marcSpecificationsByRecordTypeAndFieldTag(String recordType, String fieldTag) {
    return marcSpecificationsPath() + "/" + recordType + "/" + fieldTag;
  }

  public static String recordsEditorByIdPath(String id) {
    return recordsEditorPath() + "/" + id;
  }

  public static String sourceStoragePath(String id) {
    return SOURCE_STORAGE_RECORDS_PATH + "/" + id + "/formatted" + buildQuery("idType", "EXTERNAL");
  }

  public static String linksByInstanceIdPath(String instanceId) {
    return LINKS_INSTANCES_PATH + '/' + instanceId;
  }

  public static void mockGet(String url, String body, int status, WireMockServer mockServer) {
    mockServer.stubFor(get(urlEqualTo(url))
      .willReturn(aResponse().withBody(body)
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withStatus(status)));
  }

  public static void mockGet(String url, String body, int status, int priority, WireMockServer mockServer) {
    mockServer.stubFor(get(urlEqualTo(url))
      .atPriority(priority)
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

  private static String encodeQuery(String query) {
    return URLEncoder.encode(query, StandardCharsets.UTF_8);
  }
}
