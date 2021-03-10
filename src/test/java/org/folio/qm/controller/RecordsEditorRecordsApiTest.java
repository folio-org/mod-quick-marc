package org.folio.qm.controller;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import static org.folio.qm.utils.TestDBUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.utils.TestDBUtils.getCreationStatusById;
import static org.folio.qm.utils.TestDBUtils.saveCreationStatus;
import static org.folio.qm.utils.TestUtils.CHANGE_MANAGER_JOB_EXECUTION_PATH;
import static org.folio.qm.utils.TestUtils.CHANGE_MANAGER_JOB_PROFILE_PATH;
import static org.folio.qm.utils.TestUtils.CHANGE_MANAGER_PARSE_RECORDS_PATH;
import static org.folio.qm.utils.TestUtils.EXISTED_INSTANCE_ID;
import static org.folio.qm.utils.TestUtils.INSTANCE_ID;
import static org.folio.qm.utils.TestUtils.JOHN_TOKEN_HEADER;
import static org.folio.qm.utils.TestUtils.JOHN_TOKEN_HEADER_INVALID;
import static org.folio.qm.utils.TestUtils.PARSED_RECORD_DTO_PATH;
import static org.folio.qm.utils.TestUtils.QM_EDITED_RECORD_PATH;
import static org.folio.qm.utils.TestUtils.QM_LEADER_MISMATCH1;
import static org.folio.qm.utils.TestUtils.QM_LEADER_MISMATCH2;
import static org.folio.qm.utils.TestUtils.QM_RECORD_ID;
import static org.folio.qm.utils.TestUtils.QM_RECORD_PATH;
import static org.folio.qm.utils.TestUtils.QM_WRONG_ITEM_LENGTH;
import static org.folio.qm.utils.TestUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.utils.TestUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.utils.TestUtils.VALID_PARSED_RECORD_ID;
import static org.folio.qm.utils.TestUtils.changeManagerPath;
import static org.folio.qm.utils.TestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.utils.TestUtils.getFieldWithIndicators;
import static org.folio.qm.utils.TestUtils.getJsonObject;
import static org.folio.qm.utils.TestUtils.getQuickMarcJsonWithMinContent;
import static org.folio.qm.utils.TestUtils.mockGet;
import static org.folio.qm.utils.TestUtils.mockPost;
import static org.folio.qm.utils.TestUtils.mockPut;
import static org.folio.qm.utils.TestUtils.readQuickMarc;
import static org.folio.qm.utils.TestUtils.recordsEditorPath;
import static org.folio.qm.utils.TestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.utils.TestUtils.recordsEditorStatusPath;
import static org.folio.qm.utils.TestUtils.verifyDateTimeUpdating;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.QuickMarcFields;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.extension.ClearTable;
import org.folio.qm.util.ErrorCodes;
import org.folio.qm.util.ErrorUtils;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.folio.tenant.domain.dto.Error;

@Log4j2
class RecordsEditorRecordsApiTest extends BaseApiTest {

  @Test
  void testGetQuickMarcRecord() {
    log.info("===== Verify GET record: Successful =====");

    mockGet(changeManagerPath(INSTANCE_ID, EXISTED_INSTANCE_ID), getJsonObject(PARSED_RECORD_DTO_PATH).encode(), SC_OK,
      wireMockServer);

    QuickMarc quickMarcJson =
      verifyGet(recordsEditorPath(INSTANCE_ID, EXISTED_INSTANCE_ID), SC_OK).as(QuickMarc.class);


    assertThat(quickMarcJson.getParsedRecordDtoId(), equalTo(VALID_PARSED_RECORD_DTO_ID));
    assertThat(quickMarcJson.getInstanceId(), equalTo(EXISTED_INSTANCE_ID));
    assertThat(quickMarcJson.getSuppressDiscovery(), equalTo(Boolean.FALSE));
    assertThat(quickMarcJson.getParsedRecordId(), equalTo(VALID_PARSED_RECORD_ID));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));

    var changeManagerResponse = wireMockServer.getAllServeEvents().get(0).getResponse().getBodyAsString();
    ParsedRecordDto parsedRecordDto = new JsonObject(changeManagerResponse).mapTo(ParsedRecordDto.class);
    assertThat(parsedRecordDto.getId(), equalTo(quickMarcJson.getParsedRecordDtoId()));
  }

  @Test
  void testGetQuickMarcRecordNotFound() {
    log.info("===== Verify GET record: Record Not Found =====");

    String recordNotFoundId = UUID.randomUUID().toString();

    mockGet(changeManagerPath(INSTANCE_ID, recordNotFoundId), "Not found", SC_NOT_FOUND, wireMockServer);

    Error error = verifyGet(recordsEditorPath(INSTANCE_ID, recordNotFoundId), SC_NOT_FOUND).as(Error.class);

    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()));
    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordConverterError() {
    log.info("===== Verify GET record: Converter (quickMARC internal exception) =====");

    String instanceId = UUID.randomUUID().toString();

    mockGet(changeManagerPath(INSTANCE_ID, instanceId), "{}", SC_OK, wireMockServer);

    Error error = verifyGet(recordsEditorPath(INSTANCE_ID, instanceId), SC_UNPROCESSABLE_ENTITY).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordWithoutInstanceIdParameter() {
    log.info("===== Verify GET record: Request without instanceId =====");

    String id = UUID.randomUUID().toString();

    Error error = verifyGet(recordsEditorPath("X", id), SC_BAD_REQUEST).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  void testUpdateQuickMarcRecord() {
    log.info("===== Verify PUT record: Successful =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson, SC_ACCEPTED);

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
    log.info("===== Verify PUT record: Not found =====");
    String wrongUUID = UUID.randomUUID().toString();

    mockPut(changeManagerResourceByIdPath(wrongUUID), "{}", SC_NOT_FOUND, wireMockServer);

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(wrongUUID)
      .instanceId(EXISTED_INSTANCE_ID);

    verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson, SC_NOT_FOUND);
  }

  @Test
  void testUpdateQuickMarcRecordIdsNotEqual() {
    log.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    Error error =
      verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), quickMarcJson, SC_BAD_REQUEST)
        .as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
    assertThat(error.getCode(), equalTo("BAD_REQUEST"));
    assertThat(wireMockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidBody() {
    log.info("===== Verify PUT record: Invalid Request Body =====");

    QuickMarcFields field = getFieldWithIndicators(Collections.singletonList(" "));
    QuickMarc quickMarcJson = getQuickMarcJsonWithMinContent(field, field, field).parsedRecordDtoId(UUID.randomUUID()
      .toString())
      .parsedRecordId(VALID_PARSED_RECORD_ID)
      .instanceId(UUID.randomUUID()
        .toString());

    Error error = verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson,
      SC_UNPROCESSABLE_ENTITY).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
    assertThat(error.getCode(), equalTo(ErrorCodes.ILLEGAL_INDICATORS_NUMBER.name()));
    assertThat(wireMockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() {
    log.info("===== Verify PUT record: Invalid fixed length field items =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_WRONG_ITEM_LENGTH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    Error error =
      verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson, SC_UNPROCESSABLE_ENTITY)
        .as(Error.class);
    assertThat(error.getCode(), equalTo(ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FILED.name()));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_LEADER_MISMATCH1, QM_LEADER_MISMATCH2})
  void testUpdateQuickMarcRecordLeaderMismatch(String filename) {
    log.info("===== Verify PUT record: Leader and 008 mismatch =====");

    QuickMarc quickMarcJson = readQuickMarc(filename)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    Error error =
      verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson, SC_UNPROCESSABLE_ENTITY)
        .as(Error.class);
    assertThat(error.getCode(), equalTo(ErrorCodes.LEADER_AND_008_MISMATCHING.name()));
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testGetCreationStatus() {
    log.info("===== Verify GET record status: Successful =====");

    var id = UUID.randomUUID();
    saveCreationStatus(id, id, metadata, jdbcTemplate);
    var status = verifyGet(recordsEditorStatusPath(QM_RECORD_ID, id.toString()), SC_OK).as(CreationStatus.class);

    assertThat(status, allOf(
      hasProperty(QM_RECORD_ID, equalTo(id.toString())),
      hasProperty("status", equalTo(CreationStatus.StatusEnum.NEW))
    ));
    assertThat(status.getMetadata(), allOf(
      notNullValue(),
      hasProperty("createdAt", notNullValue())
    ));
  }

  @Test
  void testReturn404IfStatusNotFound() {
    log.info("===== Verify GET record status: Not found =====");

    var notExistedId = UUID.randomUUID().toString();
    var error = verifyGet(recordsEditorStatusPath(QM_RECORD_ID, notExistedId), SC_NOT_FOUND).as(Error.class);

    assertThat(error.getMessage(), containsString("not found"));
  }

  @Test
  void testReturn400IfQmRecordIdIsInvalid() {
    log.info("===== Verify GET record status: Parameter invalid =====");

    var invalidId = "invalid";
    var error = verifyGet(recordsEditorStatusPath(QM_RECORD_ID, invalidId), SC_BAD_REQUEST).as(Error.class);

    assertThat(error.getMessage(), containsString("Parameter 'qmRecordId' is invalid"));
  }

  @Test
  void testReturn400IfQmRecordIdIsMissing() {
    log.info("===== Verify GET record status: Parameter missing =====");

    var error = verifyGet(recordsEditorStatusPath(), SC_BAD_REQUEST).as(Error.class);

    assertThat(error.getMessage(), containsString("Parameter 'qmRecordId' is required"));
  }

  @Test
  void testReturn400IfQmRecordIdIsEmpty() {
    log.info("===== Verify GET record status: Parameter empty =====");

    var error = verifyGet(recordsEditorStatusPath(QM_RECORD_ID, ""), SC_BAD_REQUEST).as(Error.class);

    assertThat(error.getMessage(), containsString("Parameter 'qmRecordId' should be not null"));
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testPostQuickMarcValidRecordCreated() throws IOException {
    log.info("===== Verify POST record: Successful =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    String jobExecution = "mockdata/change-manager/job-execution/jobExecutionCreated.json";
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, jobExecution, wireMockServer);

    final var updateJobExecutionProfile = String.format(CHANGE_MANAGER_JOB_PROFILE_PATH, VALID_JOB_EXECUTION_ID);
    mockPut(updateJobExecutionProfile, SC_OK, wireMockServer);

    final var postRecordsPath = String.format(CHANGE_MANAGER_PARSE_RECORDS_PATH, VALID_JOB_EXECUTION_ID);
    mockPost(postRecordsPath, "", wireMockServer);
    mockPost(postRecordsPath, "", wireMockServer);

    CreationStatus response = verifyPost(recordsEditorPath(), quickMarcJson, SC_CREATED, JOHN_TOKEN_HEADER).as(CreationStatus.class);
    assertThat(response.getJobExecutionId(), equalTo(VALID_JOB_EXECUTION_ID));
    assertThat(response.getStatus(), equalTo(CreationStatus.StatusEnum.NEW));

    final var qmRecordId = UUID.fromString(response.getQmRecordId());

    sendKafkaRecord("mockdata/di-event/complete-event.json", COMPLETE_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> Assertions.assertThat(getCreationStatusById(qmRecordId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.CREATED)
      );
    var creationStatus = getCreationStatusById(qmRecordId, metadata, jdbcTemplate);
    Assertions.assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("errorMessage")
      .hasFieldOrPropertyWithValue("id", qmRecordId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED)
      .hasFieldOrPropertyWithValue("jobExecutionId", UUID.fromString(VALID_JOB_EXECUTION_ID));
  }

  @Test
  void testReturn401WhenInvalidUserId() {
    log.info("===== Verify POST record: User Id Invalid =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    String jobExecution = "mockdata/change-manager/job-execution/jobExecution_invalid_user_id.json";
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, jobExecution, SC_UNPROCESSABLE_ENTITY, wireMockServer);

    final var error= verifyPost(recordsEditorPath(), quickMarcJson, SC_UNPROCESSABLE_ENTITY, JOHN_TOKEN_HEADER).as(Error.class);
    assertThat(error.getType(), equalTo(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()));
    assertThat(error.getCode(), equalTo("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void testReturn401WhenNoHeader() {
    log.info("===== Verify POST record: Bad request =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    final var error = verifyPost(recordsEditorPath(), quickMarcJson, SC_BAD_REQUEST).as(Error.class);
    assertThat(error.getMessage(), equalTo("User id can not be null"));
  }

  @Test
  void testReturn401WhenHeader(){
    log.info("===== Verify POST record: Unauthorized =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    String jobExecution = "mockdata/change-manager/job-execution/jobExecutionCreated.json";
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, jobExecution, wireMockServer);

    final var updateJobExecutionProfile = String.format(CHANGE_MANAGER_JOB_PROFILE_PATH, VALID_JOB_EXECUTION_ID);
    mockPut(updateJobExecutionProfile, SC_OK, wireMockServer);

    final var error = verifyPost(recordsEditorPath(), quickMarcJson, SC_BAD_REQUEST, JOHN_TOKEN_HEADER_INVALID).as(Error.class);
    assertThat(error.getMessage(), equalTo("User id can not be null"));
  }
}
