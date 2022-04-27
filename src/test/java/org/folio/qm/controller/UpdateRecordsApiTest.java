package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.support.utils.APITestUtils.ACTION_ID_PARAM;
import static org.folio.qm.support.utils.APITestUtils.changeManagerJobExecutionPath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerJobProfilePath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerRecordsPath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.support.utils.APITestUtils.mockPost;
import static org.folio.qm.support.utils.APITestUtils.mockPut;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorStatusPath;
import static org.folio.qm.support.utils.DBTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectFromJson;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_AUTHORITY;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_HOLDINGS;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_INSTANCE;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_ERROR;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOB_EXECUTION_CREATED;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getFieldWithIndicators;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getFieldWithValue;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getQuickMarcJsonWithMinContent;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.MvcResult;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.RecordActionStatus;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.support.extension.ClearTable;
import org.folio.qm.support.types.IntegrationTest;

@Log4j2
@IntegrationTest
class UpdateRecordsApiTest extends BaseApiTest {

  public static Stream<Arguments> testUpdateRecordTestData() {
    return Stream.of(
      Arguments.arguments(QM_RECORD_BIB_PATH, DI_COMPLETE_INSTANCE, MarcFormat.BIBLIOGRAPHIC),
      Arguments.arguments(QM_RECORD_HOLDINGS_PATH, DI_COMPLETE_HOLDINGS, MarcFormat.HOLDINGS),
      Arguments.arguments(QM_RECORD_AUTHORITY_PATH, DI_COMPLETE_AUTHORITY, MarcFormat.AUTHORITY)
    );
  }

  @ParameterizedTest
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  @MethodSource("testUpdateRecordTestData")
  void testUpdateQuickMarcRecord(String filePath, String diCompleteMockPath, MarcFormat marcFormat) throws Exception {
    log.info("===== Verify PUT record: Successful =====");

    mockPost(changeManagerJobExecutionPath(), JOB_EXECUTION_CREATED, mockServer);
    mockPut(changeManagerJobProfilePath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);
    mockPost(changeManagerRecordsPath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);

    QuickMarc qmRecord = readQuickMarc(filePath)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.IN_PROGRESS.getValue()))
      .andReturn();

    String resultResponse = result.getResponse().getContentAsString();
    RecordActionStatus response = getObjectFromJson(resultResponse, RecordActionStatus.class);

    var actionId = response.getActionId();

    sendEventAndWaitStatusChange(actionId, ActionStatusEnum.COMPLETED, DI_COMPLETE_TOPIC_NAME, diCompleteMockPath);

    performGet(recordsEditorStatusPath(ACTION_ID_PARAM, actionId.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.actionId").value(actionId.toString()))
      .andExpect(jsonPath("$.marcFormat").value(marcFormat.getValue()))
      .andExpect(jsonPath("$.actionType").value(RecordActionStatus.ActionTypeEnum.UPDATE.getValue()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.COMPLETED.getValue()))
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.metadata").value(notNullValue()));
  }

  @ParameterizedTest
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  @MethodSource("testUpdateRecordTestData")
  void testUpdateQuickMarcRecordFailedInEvent(String filePath, String diCompleteMockPath, MarcFormat marcFormat)
    throws Exception {
    log.info("===== Verify PUT record: Successful =====");

    mockPost(changeManagerJobExecutionPath(), JOB_EXECUTION_CREATED, mockServer);
    mockPut(changeManagerJobProfilePath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);
    mockPost(changeManagerRecordsPath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);

    QuickMarc qmRecord = readQuickMarc(filePath)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    MvcResult result = performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.IN_PROGRESS.getValue()))
      .andReturn();

    String resultResponse = result.getResponse().getContentAsString();
    RecordActionStatus response = getObjectFromJson(resultResponse, RecordActionStatus.class);

    var actionId = response.getActionId();

    sendEventAndWaitStatusChange(actionId, ActionStatusEnum.ERROR, DI_ERROR_TOPIC_NAME, DI_ERROR);

    performGet(recordsEditorStatusPath(ACTION_ID_PARAM, actionId.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.actionId").value(actionId.toString()))
      .andExpect(jsonPath("$.marcFormat").value(marcFormat.getValue()))
      .andExpect(jsonPath("$.actionType").value(RecordActionStatus.ActionTypeEnum.UPDATE.getValue()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.ERROR.getValue()))
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.metadata").value(notNullValue()));
  }
//
//  @Test
//  void testUpdateQuickMarcRecordFailedInEventByOptimisticLocking() throws Exception {
//    UpdateRecordsApiTest.log.info(
//      "==== Verify PUT record: Failed in external modules due to optimistic locking ====");
//
//    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, mockServer);
//
//    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
//      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
//      .externalId(EXISTED_EXTERNAL_ID);
//
//    MvcResult result = putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
//      .andExpect(request().asyncStarted())
//      .andReturn();
//
//    var optimisticLockingErrorMessage =
//      "{ \"message\": \"Cannot update record 4f531857-a91d-433a-99ae-0372cecd07d8 because "
//        + "it has been changed (optimistic locking): Stored _version is 9, _version of request is 8\", \"severity\": "
//        + "\"ERROR\", \"code\": \"23F09\", \"where\": \"PL/pgSQL function instance_set_ol_version() line 8 at RAISE\", "
//        + "\"file\": \"pl_exec.c\", \"line\": \"3855\", \"routine\": \"exec_stmt_raise\", \"schema\": "
//        + "\"diku_mod_inventory_storage\", \"table\": \"instance\" }";
//    var expectedErrorMessage = "Cannot update record 4f531857-a91d-433a-99ae-0372cecd07d8 because"
//      + " it has been changed (optimistic locking): Stored _version is 9, _version of request is 8";
//    String eventPayload = createPayload(optimisticLockingErrorMessage);
//    sendQMKafkaRecord(eventPayload);
//    mockMvc
//      .perform(asyncDispatch(result))
//      .andExpect(status().isConflict())
//      .andDo(log())
//      .andExpect(errorMessageMatch(equalTo(expectedErrorMessage)));
//  }

  @Test
  void testUpdateQuickMarcRecordWrongUuid() throws Exception {
    UpdateRecordsApiTest.log.info("===== Verify PUT record: Not found =====");
    UUID wrongUUID = UUID.randomUUID();

    mockPut(changeManagerResourceByIdPath(wrongUUID), "{}", SC_NOT_FOUND, mockServer);

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(wrongUUID)
      .externalId(EXISTED_EXTERNAL_ID);

    mockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(wrongUUID))));

    performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
      .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateQuickMarcRecordIdsNotEqual() throws Exception {
    log.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), qmRecord)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo("Request id and entity id are not equal")));

    mockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordTagIsInvalid() throws Exception {
    log.info("===== Verify PUT record: Invalid MARC tag.The tag has alphabetic symbols =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    qmRecord.getFields().get(0).setTag("001-invalid");

    performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo("Parameter 'fields[0].tag' must match \"^[0-9]{3}$\"")));

    mockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordWithEmptyBody() throws Exception {
    log.info("===== Verify PUT record: Request with empty body =====");

    performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), null)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(containsString("Required request body is missing")));

    mockServer.verify(exactly(0),
      putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidBody() throws Exception {
    log.info("===== Verify PUT record: Invalid Request Body =====");

    var field = getFieldWithIndicators(Collections.singletonList(" "));
    var titleField = getFieldWithValue("245", "title");
    QuickMarc qmRecord =
      getQuickMarcJsonWithMinContent(field, field, titleField).parsedRecordDtoId(UUID.randomUUID())
        .marcFormat(MarcFormat.BIBLIOGRAPHIC)
        .relatedRecordVersion("1")
        .parsedRecordId(VALID_PARSED_RECORD_ID)
        .externalId(UUID.randomUUID());

    performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(containsString("Should have exactly 2 indicators")));

    mockServer.verify(exactly(0),
      putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() throws Exception {
    log.info("===== Verify PUT record: Invalid fixed length field items =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    qmRecord.getFields().stream()
      .filter(fieldItem -> fieldItem.getTag().equals("008"))
      .forEach(fieldItem -> {
        @SuppressWarnings("unchecked")
        var content = ((Map<String, Object>) fieldItem.getContent());
        content.put("Date1", "12345");
      });

    performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo("Invalid Date1 field length, must be 4 characters")));

    mockServer
      .verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordLeaderMismatch() throws Exception {
    log.info("===== Verify PUT record: Leader and 008 mismatch =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    qmRecord.getFields().stream()
      .filter(fieldItem -> fieldItem.getTag().equals("008"))
      .forEach(fieldItem -> {
        @SuppressWarnings("unchecked")
        var content = ((Map<String, Object>) fieldItem.getContent());
        content.put("Desc", "a");
      });

    performPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), qmRecord)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo("The Leader and 008 do not match")));

    mockServer.verify(exactly(0),
      putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

}
