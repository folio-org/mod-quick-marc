package org.folio.qm.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.folio.qm.converter.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.MarcUtils.decodeFromMarcDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.QuickMarcFields;
import org.folio.rest.jaxrs.model.AdditionalInfo;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.Metadata;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@Log4j2
public class TestUtils {

  public static final String QM_JSON_DIR = "mockdata/change-manager";
  public static final String PARSED_RECORD_DTO_PATH = QM_JSON_DIR + "/parsedRecordDto.json";
  public static final String PARSED_RECORD_EDGE_CASES_PATH = QM_JSON_DIR + "/parsedRecordDtoMissingWhitespaces.json";
  public static final String RESTORED_PARSED_RECORD_DTO_PATH = QM_JSON_DIR + "/parsedRecordDtoRestored.json";
  public static final String PARSED_RECORDS_DIR = "mockdata/quick-marc-json";
  public static final String QM_RECORD_PATH = PARSED_RECORDS_DIR + "/quickMarcJson.json";
  public static final String QM_EDITED_RECORD_PATH = PARSED_RECORDS_DIR + "/quickMarcJson_edited.json";
  public static final String QM_RECORD_EDGE_CASES_PATH = PARSED_RECORDS_DIR + "/quickMarcJsonMissingWhitespaces.json";
  public static final String QM_LEADER_MISMATCH1 = PARSED_RECORDS_DIR + "/quickMarcJsonLeaderMismatchValueMismatch.json";
  public static final String QM_LEADER_MISMATCH2 = PARSED_RECORDS_DIR + "/quickMarcJsonLeaderMismatchMissing008Value.json";
  public static final String QM_WRONG_ITEM_LENGTH = PARSED_RECORDS_DIR + "/quickMarcJsonWrongItemLength.json";

  public static final String VALID_PARSED_RECORD_DTO_ID = "c56b70ce-4ef6-47ef-8bc3-c470bafa0b8c";
  public static final String EXISTED_INSTANCE_ID = "b9a5f035-de63-4e2c-92c2-07240c89b817";
  public static final String VALID_PARSED_RECORD_ID = "c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1";
  public static final String TESTED_TAG_NAME = "333";

  public static final String TENANT_ID = "test";
  public static final String RECORDS_EDITOR_RECORDS_PATH = "/records-editor/records";
  public static final String RECORDS_EDITOR_RECORDS_STATUS_PATH = "/records-editor/records/status";
  public static final String CHANGE_MANAGER_PARSED_RECORDS_PATH = "/change-manager/parsedRecords";
  public static final String INSTANCE_ID = "instanceId";
  public static final String QM_RECORD_ID = "qmRecordId";

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

  public static JsonObject getMockAsJson(String fullPath) {
    try {
      return new JsonObject(getMockData(fullPath));
    } catch (IOException e) {
      log.error(e.toString());
    }
    return new JsonObject();
  }

  public static String getMockData(String path) throws IOException {
    log.info("Using mock datafile: {}", path);
    try (InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(path)) {
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

  public static QuickMarcFields getFieldWithIndicators(List<String> indicators) {
    return new QuickMarcFields().tag(TESTED_TAG_NAME).content("$333 content").indicators(indicators);
  }

  public static QuickMarc getQuickMarcJsonWithMinContent(QuickMarcFields... fields) {
    return new QuickMarc().leader("01542ccm a2200361   4500").fields(Arrays.asList(fields));
  }

  public static ParsedRecordDto getParsedRecordDtoWithMinContent(ParsedRecord parsedRecord) {
    return new ParsedRecordDto().withId(VALID_PARSED_RECORD_DTO_ID)
      .withExternalIdsHolder(new ExternalIdsHolder().withInstanceId(EXISTED_INSTANCE_ID))
      .withAdditionalInfo(new AdditionalInfo().withSuppressDiscovery(false)).withRecordType(ParsedRecordDto.RecordType.MARC)
      .withRecordState(ParsedRecordDto.RecordState.ACTUAL)
      .withMetadata(new Metadata().withUpdatedDate(new Date(1594901616879L))).withParsedRecord(parsedRecord);
  }

  public static void mockGet(String url, String body, int status, WireMockServer mockServer) {
    mockServer.stubFor(get(urlEqualTo(url))
      .willReturn(aResponse().withBody(body)
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withStatus(status)));
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

  public static QuickMarc readQuickMarс(String filename) {
    return getJsonObject(filename).mapTo(QuickMarc.class);
  }

  public static void verifyDateTimeUpdating(ParsedRecordDto changeManagerRequest) {
    var entries = JsonObject.mapFrom(changeManagerRequest.getParsedRecord().getContent());
    var entriesJsonArray = entries.getJsonArray("fields");
    @SuppressWarnings("unchecked")
    List<LinkedHashMap<String, String>> fields = (List<LinkedHashMap<String, String>>) entriesJsonArray.getList();

    String value = getUpdatingDateTime(fields);

    LocalDateTime actual = decodeFromMarcDateTime(value);

    // Compare the values of up to a minutes to prevent test failure in case of any execution delays
    assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), actual.truncatedTo(ChronoUnit.MINUTES));
  }

  public static String getUpdatingDateTime(List<LinkedHashMap<String, String>> fields) {
    for (LinkedHashMap<String, String> field : fields) {
      String extractedFieldValue = field.get(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD);
      if (Objects.nonNull(extractedFieldValue)) {
        return extractedFieldValue;
      }
    }
    return null;
  }

  public static JsonObject getJsonObject(String path) {
    try {
      return new JsonObject(getMockData(path));
    } catch (Exception e) {
      return new JsonObject();
    }
  }

}
