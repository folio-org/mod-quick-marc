package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.utils.APITestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.utils.APITestUtils.mockPut;
import static org.folio.qm.utils.APITestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_LEADER_MISMATCH1;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_LEADER_MISMATCH2;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_BIB_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_WITH_INCORRECT_TAG_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_WRONG_ITEM_LENGTH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getFieldWithIndicators;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getFieldWithValue;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;

@Log4j2
public class RecordsEditorAsyncApiTest extends BaseApiTest {

  @Test
  void testUpdateQuickMarcBibRecord() throws Exception {
    testUpdateQuickMarcRecord(QM_RECORD_BIB_PATH);
  }

  @Test
  void testUpdateQuickMarcHoldingsRecord() throws Exception {
    testUpdateQuickMarcRecord(QM_RECORD_HOLDINGS_PATH);
  }

  @Test
  void testUpdateQuickMarcAuthorityRecord() throws Exception {
    testUpdateQuickMarcRecord(QM_RECORD_AUTHORITY_PATH);
  }

  @Test
  void testUpdateQuickMarcRecordFailedInEvent() throws Exception {
    RecordsEditorAsyncApiTest.log.info("===== Verify PUT record: Failed in external modules =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
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
  void testUpdateQuickMarcRecordFailedInEventByOptimisticLocking() throws Exception {
    RecordsEditorAsyncApiTest.log.info("==== Verify PUT record: Failed in external modules due to optimistic locking ====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(request().asyncStarted())
      .andReturn();

    var optimisticLockingErrorMessage = "{ \"message\": \"Cannot update record 4f531857-a91d-433a-99ae-0372cecd07d8 because "
      + "it has been changed (optimistic locking): Stored _version is 9, _version of request is 8\", \"severity\": "
      + "\"ERROR\", \"code\": \"23F09\", \"where\": \"PL/pgSQL function instance_set_ol_version() line 8 at RAISE\", "
      + "\"file\": \"pl_exec.c\", \"line\": \"3855\", \"routine\": \"exec_stmt_raise\", \"schema\": "
      + "\"diku_mod_inventory_storage\", \"table\": \"instance\" }";
    var expectedErrorMessage = "Cannot update record 4f531857-a91d-433a-99ae-0372cecd07d8 because"
      + " it has been changed (optimistic locking): Stored _version is 9, _version of request is 8";
    String eventPayload = createPayload(optimisticLockingErrorMessage);
    sendQMKafkaRecord(eventPayload, QM_COMPLETE_TOPIC_NAME);
    mockMvc
      .perform(asyncDispatch(result))
      .andExpect(status().isConflict())
      .andDo(log())
      .andExpect(errorMessageMatch(equalTo(expectedErrorMessage)));
  }

  @Test
  void testUpdateQuickMarcRecordWrongUuid() throws Exception {
    RecordsEditorAsyncApiTest.log.info("===== Verify PUT record: Not found =====");
    UUID wrongUUID = UUID.randomUUID();

    mockPut(changeManagerResourceByIdPath(wrongUUID), "{}", SC_NOT_FOUND, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(wrongUUID)
      .externalId(EXISTED_EXTERNAL_ID);

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(wrongUUID))));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateQuickMarcRecordIdsNotEqual() throws Exception {
    log.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), quickMarcJson)
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
      .externalId(EXISTED_EXTERNAL_ID);

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo("Parameter 'fields[0].tag' must match \"^[0-9]{3}$\"")));

    wireMockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordWithEmptyBody() throws Exception {
    log.info("===== Verify PUT record: Request with empty body =====");

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(containsString("Required request body is missing")));

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidBody() throws Exception {
    log.info("===== Verify PUT record: Invalid Request Body =====");

    var field = getFieldWithIndicators(Collections.singletonList(" "));
    var titleField = getFieldWithValue("245", "title");
    QuickMarc quickMarcJson = getQuickMarcJsonWithMinContent(field, field, titleField).parsedRecordDtoId(UUID.randomUUID())
      .marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .relatedRecordVersion("1")
      .parsedRecordId(VALID_PARSED_RECORD_ID)
      .externalId(UUID.randomUUID());

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors[0].message", containsString("Should have exactly 2 indicators")));

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() throws Exception {
    log.info("===== Verify PUT record: Invalid fixed length field items =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_WRONG_ITEM_LENGTH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
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
      .externalId(EXISTED_EXTERNAL_ID);

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo("The Leader and 008 do not match")));

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  private void testUpdateQuickMarcRecord(String qmRecordMockPath) throws Exception {
    log.info("===== Verify PUT record: Successful =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(qmRecordMockPath)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(request().asyncStarted())
      .andReturn();

    String eventPayload = createPayload(null);
    sendQMKafkaRecord(eventPayload, QM_COMPLETE_TOPIC_NAME);
    mockMvc
      .perform(asyncDispatch(result))
      .andDo(log())
      .andExpect(status().isAccepted());
  }

  private String createPayload(String errorMessage) throws JsonProcessingException {
    var payload = new QmCompletedEventPayload();
    payload.setRecordId(VALID_PARSED_RECORD_DTO_ID);
    payload.setErrorMessage(errorMessage);
    return new ObjectMapper().writeValueAsString(payload);
  }

  private ResultMatcher errorMessageMatch(Matcher<String> errorMessageMatcher) {
    return jsonPath("$.message", errorMessageMatcher);
  }
}
