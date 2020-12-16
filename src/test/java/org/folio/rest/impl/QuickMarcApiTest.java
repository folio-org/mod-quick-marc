package org.folio.rest.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.HttpStatus.HTTP_BAD_REQUEST;
import static org.folio.TestSuite.wireMockServer;
import static org.folio.converter.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.converter.TestUtils.EXISTED_INSTANCE_ID;
import static org.folio.converter.TestUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.converter.TestUtils.VALID_PARSED_RECORD_ID;
import static org.folio.converter.TestUtils.getFieldWithIndicators;
import static org.folio.converter.TestUtils.getQuickMarcJsonWithMinContent;
import static org.folio.service.ChangeManagerService.INSTANCE_ID;
import static org.folio.util.ResourcePathResolver.CM_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourceByIdPath;
import static org.folio.util.ResourcePathResolver.getResourcesPath;
import static org.folio.util.ServiceUtils.buildQuery;
import static org.folio.util.ServiceUtils.decodeFromMarcDateTime;
import static org.junit.Assert.assertEquals;
import static wiremock.org.hamcrest.MatcherAssert.assertThat;
import static wiremock.org.hamcrest.Matchers.equalTo;
import static wiremock.org.hamcrest.Matchers.hasSize;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecordDto;
import org.folio.util.ErrorCodes;
import org.folio.util.ErrorUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class QuickMarcApiTest extends ApiTestBase {

  private static final Logger logger = LoggerFactory.getLogger(QuickMarcApiTest.class);

  private static final String RECORDS_EDITOR_RECORDS_PATH = "/records-editor/records";
  private static final String RECORDS_EDITOR_RECORDS_PATH_ID = RECORDS_EDITOR_RECORDS_PATH + "/%s";

  @Test
  void testGetQuickMarcRecord() {
    logger.info("===== Verify GET record: Successful =====");

    wireMockServer.stubFor(get(urlEqualTo(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, EXISTED_INSTANCE_ID)))
      .willReturn(aResponse().withBody(getJsonObject(PARSED_RECORD_DTO_PATH).encode())
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withStatus(200)));

    QuickMarcJson quickMarcJson = verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery(INSTANCE_ID, EXISTED_INSTANCE_ID), 200).as(QuickMarcJson.class);

    assertThat(quickMarcJson.getParsedRecordDtoId(), equalTo(VALID_PARSED_RECORD_DTO_ID));
    assertThat(quickMarcJson.getInstanceId(), equalTo(EXISTED_INSTANCE_ID));
    assertThat(quickMarcJson.getSuppressDiscovery(), equalTo(Boolean.FALSE));
    assertThat(quickMarcJson.getParsedRecordId(), equalTo(VALID_PARSED_RECORD_ID));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));

    ParsedRecordDto changeManagerResponse = new JsonObject(wireMockServer.getAllServeEvents()
      .get(0)
      .getResponse()
      .getBodyAsString()).mapTo(ParsedRecordDto.class);
    assertThat(changeManagerResponse.getId(), equalTo(quickMarcJson.getParsedRecordDtoId()));
  }

  @Test
  void testGetQuickMarcRecordNotFound() {
    logger.info("===== Verify GET record: Record Not Found =====");

    String recordNotFoundId = UUID.randomUUID().toString();

    wireMockServer
      .stubFor(get(urlEqualTo(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, recordNotFoundId))).willReturn(aResponse()
        .withBody(JsonObject.mapFrom(new Error()).encode())
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withStatus(HttpStatus.HTTP_NOT_FOUND.toInt())));

    Error error = verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery(INSTANCE_ID, recordNotFoundId), HttpStatus.HTTP_NOT_FOUND.toInt()).as(Error.class);

    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()));
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordConverterError() {
    logger.info("===== Verify GET record: Converter (quickMARC internal exception) =====");

    String internalServerErrorInstanceId = UUID.randomUUID()
      .toString();

    wireMockServer.stubFor(get(urlEqualTo(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, internalServerErrorInstanceId)))
      .willReturn(aResponse().withBody("{}")
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withStatus(200)));

    Error error = verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery(INSTANCE_ID, internalServerErrorInstanceId), 422).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordWithoutInstanceIdParameter() {
    logger.info("===== Verify GET record: Request without instanceId =====");

    String id = UUID.randomUUID().toString();

    Error error = verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery("X", id), 400).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  void testUpdateQuickMarcRecord() {
    logger.info("===== Verify PUT record: Successful =====");

    wireMockServer.stubFor(put(urlEqualTo(getResourceByIdPath(CM_RECORDS, VALID_PARSED_RECORD_DTO_ID)))
      .willReturn(aResponse().withStatus(202)));

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .withInstanceId(EXISTED_INSTANCE_ID);

    verifyPutRequest(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_ID), quickMarcJson, 202);

    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
    ParsedRecordDto changeManagerRequest = new JsonObject(wireMockServer.getAllServeEvents()
      .get(0)
      .getRequest()
      .getBodyAsString()).mapTo(ParsedRecordDto.class);

    verifyDateTimeUpdating(changeManagerRequest);

    assertThat(changeManagerRequest.getId(), equalTo(quickMarcJson.getParsedRecordDtoId()));
  }

  @Test
  void testUpdateQuickMarcRecordWrongUuid() {
    logger.info("===== Verify PUT record: Not found =====");
    String wrongUUID = UUID.randomUUID().toString();

    wireMockServer.stubFor(put(urlEqualTo(getResourceByIdPath(CM_RECORDS, wrongUUID))).willReturn(aResponse()
      .withBody(JsonObject.mapFrom(new Error()).encode())
      .withHeader(CONTENT_TYPE, APPLICATION_JSON)
      .withStatus(404)));

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(wrongUUID)
      .withInstanceId(EXISTED_INSTANCE_ID);

    verifyPutRequest(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_ID), quickMarcJson, 404);
  }

  @Test
  void testUpdateQuickMarcRecordIdsNotEqual() {
    logger.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .withInstanceId(EXISTED_INSTANCE_ID);

    Error error = verifyPutRequest(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_DTO_ID), quickMarcJson, 400).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
    assertThat(error.getCode(), equalTo(HTTP_BAD_REQUEST.name()));
    assertThat(wireMockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidBody() {
    logger.info("===== Verify PUT record: Invalid Request Body =====");

    Field field = getFieldWithIndicators(Collections.singletonList(" "));
    QuickMarcJson quickMarcJson = getQuickMarcJsonWithMinContent(field, field, field).withParsedRecordDtoId(UUID.randomUUID()
      .toString())
      .withParsedRecordId(VALID_PARSED_RECORD_ID)
      .withInstanceId(UUID.randomUUID()
        .toString());

    Error error = verifyPutRequest(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_ID), quickMarcJson, HttpStatus.HTTP_UNPROCESSABLE_ENTITY.toInt()).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
    assertThat(error.getCode(), equalTo(ErrorCodes.ILLEGAL_INDICATORS_NUMBER.name()));
    assertThat(wireMockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() {
    logger.info("===== Verify PUT record: Invalid fixed length field items =====");

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_WRONG_ITEM_LENGTH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .withInstanceId(EXISTED_INSTANCE_ID);

    Error error = verifyPutRequest(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_ID), quickMarcJson, 422).as(Error.class);
    assertThat(error.getCode(), equalTo(ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FILED.name()));
  }

  @ParameterizedTest
  @ValueSource(strings = { QUICK_MARC_LEADER_MISMATCH1, QUICK_MARC_LEADER_MISMATCH2 })
  void testUpdateQuickMarcRecordLeaderMismatch(String filename) {
    logger.info("===== Verify PUT record: Leader and 008 mismatch =====");

    QuickMarcJson quickMarcJson = getJsonObject(filename).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .withInstanceId(EXISTED_INSTANCE_ID);

    Error error = verifyPutRequest(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_ID), quickMarcJson, 422).as(Error.class);
    assertThat(error.getCode(), equalTo(ErrorCodes.LEADER_AND_008_MISMATCHING.name()));
  }

  private void verifyDateTimeUpdating(ParsedRecordDto changeManagerRequest) {
    List<LinkedHashMap<String, String>> fields
      = (List<LinkedHashMap<String, String>>) JsonObject.mapFrom(changeManagerRequest.getParsedRecord().getContent()).getJsonArray("fields").getList();

    String value = getUpdatingDateTime(fields);

    LocalDateTime actual = decodeFromMarcDateTime(value);

    // Compare the values of up to a minutes to prevent test failure in case of any execution delays
    assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), actual.truncatedTo(ChronoUnit.MINUTES));
  }

  private String getUpdatingDateTime(List<LinkedHashMap<String, String>> fields) {
    for (LinkedHashMap<String, String> field : fields) {
      String extractedFieldValue = field.get(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD);
      if (Objects.nonNull(extractedFieldValue)) {
        return extractedFieldValue;
      }
    }
    return null;
  }
}
