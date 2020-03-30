package org.folio.rest.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.folio.TestSuite.wireMockServer;
import static org.folio.util.ResourcePathResolver.SRS_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourcesPath;
import static org.folio.util.ServiceUtils.buildQuery;
import static wiremock.org.hamcrest.MatcherAssert.assertThat;
import static wiremock.org.hamcrest.Matchers.hasSize;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickMarcApiTest extends ApiTestBase {

  private static final Logger logger = LoggerFactory.getLogger(QuickMarcApiTest.class);
  private static final String EXISTED_INSTANCE_ID = "54cc0262-76df-4cac-acca-b10e9bc5c79a";
  private static final String CONTENT_TYPE = "Content-type";
  private static final String RECORDS_EDITOR_RECORDS_PATH = "/records-editor/records";

  @Test
  public void testGetQuickMarcRecord() {
    logger.info("===== Verify GET record: Successful =====");

    wireMockServer
      .stubFor(get(urlEqualTo(getResourcesPath(SRS_RECORDS) + buildQuery("query","externalIdsHolder.instanceId==" + EXISTED_INSTANCE_ID)))
        .willReturn(aResponse().withBody(getSrsRecordsBySearchParameter(EXISTED_INSTANCE_ID).encode())
          .withHeader(CONTENT_TYPE, APPLICATION_JSON)
          .withStatus(200)));

    verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery("instanceId", EXISTED_INSTANCE_ID), 200);
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  public void testGetQuickMarcRecordNotFound() {
    logger.info("===== Verify GET record: Record Not Found =====");

    String recordNotFoundId = UUID.randomUUID().toString();

    wireMockServer
      .stubFor(get(urlEqualTo(getResourcesPath(SRS_RECORDS) + buildQuery("query","externalIdsHolder.instanceId==" + recordNotFoundId)))
        .willReturn(aResponse()
          .withBody(getSrsRecordsBySearchParameter(recordNotFoundId).encode())
          .withHeader("Content-type", APPLICATION_JSON)
          .withStatus(200)));

    verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery("instanceId", recordNotFoundId), 404);
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  public void testGetQuickMarcRecordInternalServerError() {
    logger.info("===== Verify GET record: Internal Server Error =====");

    String internalServerErrorInstanceId = UUID.randomUUID().toString();

    wireMockServer.stubFor(get(
        urlEqualTo(getResourcesPath(SRS_RECORDS) + buildQuery("query","externalIdsHolder.instanceId==" + internalServerErrorInstanceId)))
          .willReturn(aResponse().withBody("Internal server error")
            .withHeader("Content-type", TEXT_PLAIN)
            .withStatus(500)));

    verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery("instanceId", internalServerErrorInstanceId), 500);
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  public void testGetQuickMarcRecordWithoutInstanceIdParameter() {
    logger.info("===== Verify GET record: Request without instanceId =====");

    String id = UUID.randomUUID().toString();

    verifyGetRequest(RECORDS_EDITOR_RECORDS_PATH + buildQuery("X", id), 400);
  }
}
