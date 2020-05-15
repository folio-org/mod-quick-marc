package org.folio.rest.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.folio.TestSuite.wireMockServer;
import static org.folio.util.Constants.INSTANCE_ID;
import static org.folio.util.ResourcePathResolver.CM_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourceByIdPath;
import static org.folio.util.ResourcePathResolver.getResourcesPath;
import static org.folio.util.ServiceUtils.buildQuery;
import static wiremock.org.hamcrest.MatcherAssert.assertThat;
import static wiremock.org.hamcrest.Matchers.equalTo;
import static wiremock.org.hamcrest.Matchers.hasSize;

import java.util.UUID;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickMarcApiTest extends ApiTestBase {

  private static final Logger logger = LoggerFactory.getLogger(QuickMarcApiTest.class);
  private static final String EXISTED_INSTANCE_ID = "54cc0262-76df-4cac-acca-b10e9bc5c79a";
  private static final String VALID_PARSED_RECORD_ID = "c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1";
  private static final String VALID_PARSED_RECORD_DTO_ID = "67dfac11-1caf-4470-9ad1-d533f6360bdd";
  private static final String VALID_INSTANCE_ID = "54cc0262-76df-4cac-acca-b10e9bc5c79a";
  private static final String CONTENT_TYPE = "Content-type";
  private static final String RECORDS_EDITOR_RECORDS_PATH = "/records-editor/records";
  private static final String RECORDS_EDITOR_RECORDS_PATH_ID = RECORDS_EDITOR_RECORDS_PATH + "/%s";
  private static final String INVALID_UUID = "invalid UUID";

  @Test
  public void testGetQuickMarcRecord() {
    logger.info("===== Verify GET record: Successful =====");

    wireMockServer
      .stubFor(get(urlEqualTo(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, EXISTED_INSTANCE_ID)))
        .willReturn(aResponse().withBody(getJsonObject(PARSED_RECORD_DTO_PATH).encode())
          .withHeader(CONTENT_TYPE, APPLICATION_JSON)
          .withStatus(200)));

    QuickMarcJson quickMarcJson = verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery(INSTANCE_ID, EXISTED_INSTANCE_ID), 200)
      .as(QuickMarcJson.class);
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
    assertThat(quickMarcJson.getParsedRecordDtoId(), equalTo(VALID_PARSED_RECORD_DTO_ID));
    assertThat(quickMarcJson.getInstanceId(), equalTo(VALID_INSTANCE_ID));
    assertThat(quickMarcJson.getSuppressDiscovery(), equalTo(Boolean.FALSE));
    assertThat(quickMarcJson.getParsedRecordId(), equalTo(VALID_PARSED_RECORD_ID));
  }

  @Test
  public void testGetQuickMarcRecordNotFound() {
    logger.info("===== Verify GET record: Record Not Found =====");

    String recordNotFoundId = UUID.randomUUID().toString();

    wireMockServer
      .stubFor(get(urlEqualTo(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, recordNotFoundId)))
        .willReturn(aResponse()
          .withBody(JsonObject.mapFrom(new Error()).encode())
          .withHeader(CONTENT_TYPE, APPLICATION_JSON)
          .withStatus(404)));

    verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery(INSTANCE_ID, recordNotFoundId), 404);
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  public void testGetQuickMarcRecordInternalServerError() {
    logger.info("===== Verify GET record: Internal Server Error =====");

    String internalServerErrorInstanceId = UUID.randomUUID().toString();

    wireMockServer.stubFor(get(
        urlEqualTo(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, internalServerErrorInstanceId)))
          .willReturn(aResponse().withBody("Internal server error")
            .withHeader(CONTENT_TYPE, TEXT_PLAIN)
            .withStatus(500)));

    verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery(INSTANCE_ID, internalServerErrorInstanceId), 500);
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  public void testGetQuickMarcRecordWithoutInstanceIdParameter() {
    logger.info("===== Verify GET record: Request without instanceId =====");

    String id = UUID.randomUUID().toString();

    verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery("X", id), 400);
  }

  @Test
  public void testUpdateQuickMarcRecord() {
    logger.info("===== Verify PUT record: Successful =====");

    wireMockServer
      .stubFor(put(urlEqualTo(getResourceByIdPath(CM_RECORDS, VALID_PARSED_RECORD_DTO_ID)))
        .withRequestBody(containing(getJsonObject(UPDATED_RECORD_PATH).encode()))
        .willReturn(aResponse().withStatus(204)));

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .withInstanceId(VALID_INSTANCE_ID);
    verifyPut(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_ID), quickMarcJson, 204);
  }

  @Test
  public void testUpdateQuickMarcRecordWrongUuid() {
    logger.info("===== Verify PUT record: Not found =====");
    String wrongUUID = UUID.randomUUID().toString();

    wireMockServer
      .stubFor(put(urlEqualTo(getResourceByIdPath(CM_RECORDS, wrongUUID)))
        .withRequestBody(containing(getJsonObject(UPDATED_RECORD_PATH).encode()))
        .willReturn(aResponse().withStatus(404)));

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(wrongUUID)
      .withInstanceId(VALID_INSTANCE_ID);
    verifyPut(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_ID), quickMarcJson, 404);
  }

  @Test
  public void testUpdateQuickMarcRecordIdsNotEqual() {
    logger.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .withInstanceId(VALID_INSTANCE_ID);
    verifyPut(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_DTO_ID), quickMarcJson, 400);
  }

  @Test
  public void testUpdateQuickMarcRecordInvalidUuid() {
    logger.info("===== Verify PUT record: Invalid UUID =====");

    QuickMarcJson quickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class)
      .withParsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .withInstanceId(VALID_INSTANCE_ID);
    verifyPut(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, INVALID_UUID), quickMarcJson, 400);
  }

  @Test
  public void testUpdateQuickMarcRecordInvalidBody() {
    logger.info("===== Verify PUT record: Invalid Request Body =====");

    QuickMarcJson invalidQuickMarcJson = getJsonObject(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class);
    verifyPut(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, VALID_PARSED_RECORD_DTO_ID), invalidQuickMarcJson, 422);
  }

  @Test
  public void testUpdateQuickMarcRecordInvalidField008() {
    logger.info("===== Verify PUT record: Invalid Field 008 Items =====");

    QuickMarcJson invalidQuickMarcJson = getJsonObject(INVALID_QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class);
    verifyPut(String.format(RECORDS_EDITOR_RECORDS_PATH_ID, INVALID_UUID), invalidQuickMarcJson, 422);
  }
}
