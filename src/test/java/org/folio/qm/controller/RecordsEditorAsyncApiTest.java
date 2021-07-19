package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.utils.APITestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.utils.APITestUtils.mockPut;
import static org.folio.qm.utils.APITestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.EXISTED_INSTANCE_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.spring.integration.XOkapiHeaders;

@Log4j2
@AutoConfigureMockMvc
public class RecordsEditorAsyncApiTest extends BaseApiTest {

  @Autowired
  protected MockMvc mockMvc;

  @Test
  void testUpdateQuickMarcRecord() throws Exception {
    log.info("===== Verify PUT record: Successful =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    MvcResult result = mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andExpect(request().asyncStarted())
      .andReturn();

    String eventPayload = createPayload(null);
    sendKafkaRecord(eventPayload, QM_COMPLETE_TOPIC_NAME);
    var mvcResult = mockMvc
      .perform(asyncDispatch(result))
      .andExpect(status().isAccepted())
      .andReturn();
    System.out.println(mvcResult);
  }

  @Test
  void testUpdateQuickMarcRecordFailedInEvent() throws Exception {
    log.info("===== Verify PUT record: Failed in external modules =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    MvcResult result = mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andExpect(request().asyncStarted())
      .andReturn();

    var errorMessage = "Some error occurred";
    String eventPayload = createPayload(errorMessage);
    sendKafkaRecord(eventPayload, QM_COMPLETE_TOPIC_NAME);
    mockMvc
      .perform(asyncDispatch(result))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateQuickMarcRecordWrongUuid() throws Exception {
    log.info("===== Verify PUT record: Not found =====");
    String wrongUUID = UUID.randomUUID().toString();

    mockPut(changeManagerResourceByIdPath(wrongUUID), "{}", SC_NOT_FOUND, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(wrongUUID)
      .instanceId(EXISTED_INSTANCE_ID);

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(wrongUUID))));

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andExpect(status().isNotFound());
  }

  private String createPayload(String errorMessage) throws JsonProcessingException {
    var payload = new QmCompletedEventPayload();
    payload.setRecordId(VALID_PARSED_RECORD_ID);
    payload.setErrorMessage(errorMessage);
    return new ObjectMapper().writeValueAsString(payload);
  }

  private HttpHeaders defaultHeaders() {
    final HttpHeaders httpHeaders = new HttpHeaders();

    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.add(XOkapiHeaders.TENANT, TENANT_ID);
    httpHeaders.add(XOkapiHeaders.USER_ID, JOHN_USER_ID);
    httpHeaders.add(XOkapiHeaders.URL, getOkapiUrl());

    return httpHeaders;
  }

  private ResultMatcher errorMessageMatch(Matcher<String> errorMessageMatcher) {
    return jsonPath("$.message", errorMessageMatcher);
  }
}
