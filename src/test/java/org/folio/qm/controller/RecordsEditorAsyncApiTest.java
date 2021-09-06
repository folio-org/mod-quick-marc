package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.utils.APITestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.utils.APITestUtils.mockPut;
import static org.folio.qm.utils.APITestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_LEADER_MISMATCH1;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_LEADER_MISMATCH2;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_WITH_INCORRECT_TAG_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_WRONG_ITEM_LENGTH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getFieldWithIndicators;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getQuickMarcJsonWithMinContent;

import java.util.Collections;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.QuickMarcFields;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.spring.integration.XOkapiHeaders;

@Log4j2
public class RecordsEditorAsyncApiTest extends BaseApiTest {

  @Test
  void testUpdateQuickMarcRecord() throws Exception {
    RecordsEditorAsyncApiTest.log.info("===== Verify PUT record: Successful =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_EXTERNAL_ID);

    MvcResult result = mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andExpect(request().asyncStarted())
      .andReturn();

    String eventPayload = createPayload(null);
    sendQMKafkaRecord(eventPayload, QM_COMPLETE_TOPIC_NAME);
    mockMvc
      .perform(asyncDispatch(result))
      .andDo(log())
      .andExpect(status().isAccepted());
  }

  @Test
  void testUpdateQuickMarcRecordFailedInEvent() throws Exception {
    RecordsEditorAsyncApiTest.log.info("===== Verify PUT record: Failed in external modules =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_EXTERNAL_ID);

    MvcResult result = mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andExpect(request().asyncStarted())
      .andReturn();

    var errorMessage = "Some error occurred";
    String eventPayload = createPayload(errorMessage);
    sendQMKafkaRecord(eventPayload, QM_COMPLETE_TOPIC_NAME);
    mockMvc
      .perform(asyncDispatch(result))
      .andExpect(status().isBadRequest())
      .andDo(log())
      .andExpect(errorMessageMatch(equalTo(errorMessage)));
  }

  @Test
  void testUpdateQuickMarcRecordWrongUuid() throws Exception {
    RecordsEditorAsyncApiTest.log.info("===== Verify PUT record: Not found =====");
    String wrongUUID = UUID.randomUUID().toString();

    mockPut(changeManagerResourceByIdPath(wrongUUID), "{}", SC_NOT_FOUND, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(wrongUUID)
      .instanceId(EXISTED_EXTERNAL_ID);

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(wrongUUID))));

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andDo(log())
      .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateQuickMarcRecordIdsNotEqual() throws Exception {
    log.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_EXTERNAL_ID);

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andDo(log())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo("Request id and entity id are not equal")));

    wireMockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordTagIsInvalid() throws Exception {
    log.info("===== Verify PUT record: Invalid MARC tag.The tag has alphabetic symbols =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_WITH_INCORRECT_TAG_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_EXTERNAL_ID);

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andDo(log())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo("Parameter 'fields[0].tag' must match \"^[0-9]{3}$\"")));

    wireMockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordWithEmptyBody() throws Exception {
    log.info("===== Verify PUT record: Request with empty body =====");

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(""))
      .andDo(log())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(containsString("Required request body is missing")));

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidBody() throws Exception {
    log.info("===== Verify PUT record: Invalid Request Body =====");

    QuickMarcFields field = getFieldWithIndicators(Collections.singletonList(" "));
    QuickMarc quickMarcJson = getQuickMarcJsonWithMinContent(field, field, field).parsedRecordDtoId(UUID.randomUUID()
      .toString())
      .parsedRecordId(VALID_PARSED_RECORD_ID)
      .instanceId(UUID.randomUUID()
        .toString());

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andDo(log())
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo("Illegal indicators number for field: 333")));

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() throws Exception {
    log.info("===== Verify PUT record: Invalid fixed length field items =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_WRONG_ITEM_LENGTH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_EXTERNAL_ID);

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andDo(log())
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo("Invalid Date1 field length, must be 4 characters")));

    wireMockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_LEADER_MISMATCH1, QM_LEADER_MISMATCH2})
  void testUpdateQuickMarcRecordLeaderMismatch(String filename) throws Exception {
    log.info("===== Verify PUT record: Leader and 008 mismatch =====");

    QuickMarc quickMarcJson = readQuickMarc(filename)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_EXTERNAL_ID);

    mockMvc.perform(put(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(quickMarcJson)))
      .andDo(log())
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo("The Leader and 008 do not match")));

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  private String createPayload(String errorMessage) throws JsonProcessingException {
    var payload = new QmCompletedEventPayload();
    payload.setRecordId(VALID_PARSED_RECORD_DTO_ID);
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
