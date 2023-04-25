package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.qm.support.utils.ApiTestUtils.CHANGE_MANAGER_JOB_EXECUTION_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.CHANGE_MANAGER_JOB_PROFILE_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.CHANGE_MANAGER_PARSE_RECORDS_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.EXTERNAL_ID;
import static org.folio.qm.support.utils.ApiTestUtils.changeManagerPath;
import static org.folio.qm.support.utils.ApiTestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.support.utils.ApiTestUtils.linksByInstanceIdPath;
import static org.folio.qm.support.utils.ApiTestUtils.mockGet;
import static org.folio.qm.support.utils.ApiTestUtils.mockPost;
import static org.folio.qm.support.utils.ApiTestUtils.mockPut;
import static org.folio.qm.support.utils.ApiTestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.support.utils.DataBaseTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.support.utils.InputOutputTestUtils.readFile;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_AUTHORITY_DELETE;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOB_EXECUTION_CREATED;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getFieldWithIndicators;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getFieldWithValue;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getQuickMarcJsonWithMinContent;
import static org.folio.qm.validation.FieldValidationRule.IS_REQUIRED_TAG_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.qm.support.extension.ClearTable;
import org.folio.qm.support.types.IntegrationTest;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

@Log4j2
@IntegrationTest
class RecordsEditorAsyncIT extends BaseIT {

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_BIB_PATH, QM_RECORD_HOLDINGS_PATH, QM_RECORD_AUTHORITY_PATH})
  void testUpdateQuickMarcRecord(String filePath) throws Exception {
    log.info("===== Verify PUT record: Successful =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    if (filePath.equals(QM_RECORD_BIB_PATH)) {
      mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);
    }

    QuickMarc quickMarcJson = readQuickMarc(filePath)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(request().asyncStarted())
      .andReturn();

    String eventPayload = createPayload(null);
    sendQuickMarcKafkaRecord(eventPayload);
    mockMvc
      .perform(asyncDispatch(result))
      .andDo(log())
      .andExpect(status().isAccepted());

    if (filePath.equals(QM_RECORD_BIB_PATH)) {
      wireMockServer.verify(exactly(1), putRequestedFor(urlEqualTo(linksByInstanceIdPath(EXISTED_EXTERNAL_ID))));
    } else {
      wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(linksByInstanceIdPath(EXISTED_EXTERNAL_ID))));
    }
  }

  @Test
  void testUpdateQuickMarcRecordFailedInEvent() throws Exception {
    log.info("===== Verify PUT record: Failed in external modules =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(request().asyncStarted())
      .andReturn();

    var errorMessage = "Some error occurred";
    String eventPayload = createPayload(errorMessage);
    sendQuickMarcKafkaRecord(eventPayload);
    mockMvc
      .perform(asyncDispatch(result))
      .andExpect(status().isBadRequest())
      .andDo(log())
      .andExpect(errorMessageMatch(equalTo(errorMessage)));

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(linksByInstanceIdPath(EXISTED_EXTERNAL_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordFailedInEventByOptimisticLocking() throws Exception {
    log.info("==== Verify PUT record: Failed in external modules due to optimistic locking ====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(request().asyncStarted())
      .andReturn();

    String eventPayload = createPayload(null);
    sendQuickMarcKafkaRecord(eventPayload);
    mockMvc
      .perform(asyncDispatch(result))
      .andDo(log())
      .andExpect(status().isAccepted());

    MvcResult result2 = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(request().asyncStarted())
      .andReturn();

    var expectedErrorMessage = "Cannot update record 4f531857-a91d-433a-99ae-0372cecd07d8 because"
      + " it has been changed (optimistic locking): Stored _version is 9, _version of request is 8";
    String eventPayload2 = createPayload(expectedErrorMessage);
    sendQuickMarcKafkaRecord(eventPayload2);
    mockMvc
      .perform(asyncDispatch(result2))
      .andExpect(status().isConflict())
      .andDo(log())
      .andExpect(errorMessageMatch(equalTo(expectedErrorMessage)));

    wireMockServer.verify(exactly(1), putRequestedFor(urlEqualTo(linksByInstanceIdPath(EXISTED_EXTERNAL_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordWrongUuid() throws Exception {
    log.info("===== Verify PUT record: Not found =====");
    UUID wrongUuid = UUID.randomUUID();

    mockPut(changeManagerResourceByIdPath(wrongUuid), "{}", SC_NOT_FOUND, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(wrongUuid)
      .externalId(EXISTED_EXTERNAL_ID);

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(wrongUuid))));

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

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    quickMarcJson.getFields().get(0).setTag("001-invalid");

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

    wireMockServer.verify(exactly(0),
      putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidBody() throws Exception {
    log.info("===== Verify PUT record: Invalid Request Body =====");

    var field = getFieldWithIndicators(Collections.singletonList(" "));
    var titleField = getFieldWithValue("245", "title");
    QuickMarc quickMarcJson =
      getQuickMarcJsonWithMinContent(field, field, titleField).parsedRecordDtoId(UUID.randomUUID())
        .marcFormat(MarcFormat.BIBLIOGRAPHIC)
        .relatedRecordVersion("1")
        .parsedRecordId(VALID_PARSED_RECORD_ID)
        .externalId(UUID.randomUUID());

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.message", containsString("Should have exactly 2 indicators")));

    wireMockServer.verify(exactly(0),
      putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() throws Exception {
    log.info("===== Verify PUT record: Invalid fixed length field items =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    quickMarcJson.getFields().stream()
      .filter(fieldItem -> fieldItem.getTag().equals("008"))
      .forEach(fieldItem -> {
        @SuppressWarnings("unchecked")
        var content = (Map<String, Object>) fieldItem.getContent();
        content.put("Date1", "12345");
      });

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo("Invalid Date1 field length, must be 4 characters")));

    wireMockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordIgnoreElvlLeaderMismatch() throws Exception {
    log.info("===== Verify PUT record: Leader and ignore 008 Elvl mismatch =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    quickMarcJson.getFields().stream()
      .filter(fieldItem -> fieldItem.getTag().equals("008"))
      .forEach(fieldItem -> {
        @SuppressWarnings("unchecked")
        var content = (Map<String, Object>) fieldItem.getContent();
        content.put("Elvl", "a");
      });

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_BIB_PATH, QM_RECORD_HOLDINGS_PATH, QM_RECORD_AUTHORITY_PATH})
  void testUpdateReturn422WhenRecordWithMultiple001(String filePath) throws Exception {
    log.info("===== Verify PUT record: 001 tag check =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(filePath)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    // Now we add the new 001 field to the record and try to update existing record
    quickMarcJson.getFields().add(new FieldItem().tag("001").content("$a test value"));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo(IS_UNIQUE_TAG_ERROR_MSG)));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_BIB_PATH, QM_RECORD_HOLDINGS_PATH, QM_RECORD_AUTHORITY_PATH})
  void testUpdateReturn422WhenRecordMissed008(String filePath) throws Exception {
    log.info("===== Verify PUT record: 008 tag check =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(filePath)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    // Now we remove the 008 field from the record and try to update existing record
    quickMarcJson.getFields().removeIf(field -> field.getTag().equals("008"));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo(IS_REQUIRED_TAG_ERROR_MSG)));
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testDeleteQuickMarcAuthorityRecord() throws Exception {
    log.info("===== Verify DELETE authority record: No Content");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_AUTHORITY_DTO_PATH), SC_OK,
      wireMockServer);

    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, JOB_EXECUTION_CREATED, wireMockServer);

    final var updateJobExecutionProfile = String.format(CHANGE_MANAGER_JOB_PROFILE_PATH, VALID_JOB_EXECUTION_ID);
    mockPut(updateJobExecutionProfile, SC_OK, wireMockServer);

    final var postRecordsPath = String.format(CHANGE_MANAGER_PARSE_RECORDS_PATH, VALID_JOB_EXECUTION_ID);
    mockPost(postRecordsPath, "", wireMockServer);

    MvcResult result = deleteResultActions(recordsEditorResourceByIdPath(EXISTED_EXTERNAL_ID))
      .andExpect(request().asyncStarted())
      .andReturn();

    sendDataImportKafkaRecord(DI_COMPLETE_AUTHORITY_DELETE, DI_COMPLETE_TOPIC_NAME);

    mockMvc
      .perform(asyncDispatch(result))
      .andDo(log())
      .andExpect(status().isNoContent());
  }

  @ParameterizedTest
  @ValueSource(strings = {PARSED_RECORD_BIB_DTO_PATH, PARSED_RECORD_HOLDINGS_DTO_PATH})
  void testDeleteQuickMarcNoAuthorityRecord(String filename) throws Exception {
    log.info("===== Verify DELETE no authority record: Bad Request");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(filename), SC_OK,
      wireMockServer);

    deleteResultActions(recordsEditorResourceByIdPath(EXISTED_EXTERNAL_ID))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value(containsString("Job profile for [DELETE] action")));
  }

  @Test
  void testDeleteQuickMarcRecordWrongUuid() throws Exception {
    RecordsEditorAsyncIT.log.info("===== Verify DELETE record: Not found =====");
    mockGet(changeManagerPath(EXTERNAL_ID, VALID_PARSED_RECORD_ID), "{}", SC_NOT_FOUND, wireMockServer);

    wireMockServer.verify(exactly(0),
      getRequestedFor(urlEqualTo(changeManagerPath(EXTERNAL_ID, VALID_PARSED_RECORD_ID))));

    deleteResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .andExpect(status().isNotFound());
  }

  private String createPayload(String errorMessage) throws JsonProcessingException {
    var payload = new QmCompletedEventPayload();
    payload.setRecordId(VALID_PARSED_RECORD_ID);
    payload.setErrorMessage(errorMessage);
    return new ObjectMapper().writeValueAsString(payload);
  }

  private ResultMatcher errorMessageMatch(Matcher<String> errorMessageMatcher) {
    return jsonPath("$.message", errorMessageMatcher);
  }
}
