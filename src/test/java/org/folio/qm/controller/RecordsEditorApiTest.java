package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.awaitility.Awaitility.await;
import static org.folio.qm.utils.APITestUtils.CHANGE_MANAGER_JOB_EXECUTION_PATH;
import static org.folio.qm.utils.APITestUtils.CHANGE_MANAGER_JOB_PROFILE_PATH;
import static org.folio.qm.utils.APITestUtils.CHANGE_MANAGER_PARSE_RECORDS_PATH;
import static org.folio.qm.utils.APITestUtils.EXTERNAL_ID;
import static org.folio.qm.utils.APITestUtils.JOHN_USER_ID_HEADER;
import static org.folio.qm.utils.APITestUtils.QM_RECORD_ID;
import static org.folio.qm.utils.APITestUtils.changeManagerPath;
import static org.folio.qm.utils.APITestUtils.mockGet;
import static org.folio.qm.utils.APITestUtils.mockPost;
import static org.folio.qm.utils.APITestUtils.mockPut;
import static org.folio.qm.utils.APITestUtils.recordsEditorPath;
import static org.folio.qm.utils.APITestUtils.recordsEditorStatusPath;
import static org.folio.qm.utils.APITestUtils.usersByIdPath;
import static org.folio.qm.utils.JsonTestUtils.getObjectFromJson;
import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.folio.qm.utils.DBTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.utils.DBTestUtils.getCreationStatusById;
import static org.folio.qm.utils.DBTestUtils.saveCreationStatus;
import static org.folio.qm.utils.IOTestUtils.readFile;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_HRID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_EDITED_RECORD_BIB_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_EDITED_RECORD_HOLDINGS_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.USER_JOHN_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;

import java.util.UUID;
import java.util.regex.Pattern;

import com.github.tomakehurst.wiremock.http.Fault;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.extension.ClearTable;
import org.folio.qm.util.ErrorUtils;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.springframework.test.web.servlet.MvcResult;

@Log4j2
class RecordsEditorApiTest extends BaseApiTest {

  @Test
  void testGetQuickMarcBibRecord() throws Exception {
    log.info("===== Verify GET Bibliographic record: Successful =====");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_BIB_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.marcFormat").value(MarcFormat.BIBLIOGRAPHIC.getValue()))
      .andExpect(jsonPath("$.parsedRecordDtoId").value(VALID_PARSED_RECORD_DTO_ID.toString()))
      .andExpect(jsonPath("$.externalId").value(EXISTED_EXTERNAL_ID.toString()))
      .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
      .andExpect(jsonPath("$.parsedRecordId").value(VALID_PARSED_RECORD_ID.toString()))
      .andExpect(jsonPath("$.updateInfo.updatedBy.userId").value(JOHN_USER_ID));

    checkParseRecordDtoId();
  }

  @Test
  void testGetQuickMarcHoldingsRecord() throws Exception {
    log.info("===== Verify GET Holdings record: Successful =====");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_HOLDINGS_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.marcFormat").value(MarcFormat.HOLDINGS.getValue()))
      .andExpect(jsonPath("$.parsedRecordDtoId").value(VALID_PARSED_RECORD_DTO_ID.toString()))
      .andExpect(jsonPath("$.externalId").value(EXISTED_EXTERNAL_ID.toString()))
      .andExpect(jsonPath("$.externalHrid").value(EXISTED_EXTERNAL_HRID))
      .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
      .andExpect(jsonPath("$.parsedRecordId").value(VALID_PARSED_RECORD_ID.toString()))
      .andExpect(jsonPath("$.updateInfo.updatedBy.userId").value(JOHN_USER_ID));

    checkParseRecordDtoId();
  }

  @Test
  void testGetQuickMarcAuthorityRecord() throws Exception {
    log.info("===== Verify GET Authority record: Successful =====");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_AUTHORITY_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.marcFormat").value(MarcFormat.AUTHORITY.getValue()))
      .andExpect(jsonPath("$.parsedRecordDtoId").value(VALID_PARSED_RECORD_DTO_ID.toString()))
      .andExpect(jsonPath("$.externalId").value(EXISTED_EXTERNAL_ID.toString()))
      .andExpect(jsonPath("$.externalHrid").doesNotExist())
      .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
      .andExpect(jsonPath("$.parsedRecordId").value(VALID_PARSED_RECORD_ID.toString()))
      .andExpect(jsonPath("$.updateInfo.updatedBy.userId").value(JOHN_USER_ID));

    checkParseRecordDtoId();
  }

  @Test
  void testGetQuickMarcRecordNotFound() throws Exception {
    log.info("===== Verify GET record: Record Not Found =====");

    UUID recordNotFoundId = UUID.randomUUID();

    mockGet(changeManagerPath(EXTERNAL_ID, recordNotFoundId), "Not found", SC_NOT_FOUND, wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, recordNotFoundId))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordConverterError() throws Exception {
    log.info("===== Verify GET record: Converter (quickMARC internal exception) =====");

    UUID instanceId = UUID.randomUUID();

    mockGet(changeManagerPath(EXTERNAL_ID, instanceId), "{\"recordType\": \"MARC_BIB\"}", SC_OK, wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, instanceId))
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordWithoutInstanceIdParameter() throws Exception {
    log.info("===== Verify GET record: Request without instanceId =====");

    UUID id = UUID.randomUUID();

    getResultActions(recordsEditorPath("X", id))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    assertThat(wireMockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testGetCreationStatus() throws Exception {
    log.info("===== Verify GET record status: Successful =====");

    var id = UUID.randomUUID();
    saveCreationStatus(id, id, metadata, jdbcTemplate);

    getResultActions(recordsEditorStatusPath(QM_RECORD_ID, id.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.qmRecordId").value(id.toString()))
      .andExpect(jsonPath("$.status").value(CreationStatus.StatusEnum.NEW.getValue()))
      .andExpect(jsonPath("$.metadata").value(notNullValue()))
      .andExpect(jsonPath("$.metadata.createdAt").value(notNullValue()));
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testGetCreationStatusHasProperlyFormattedDate() throws Exception {
    log.info("===== Verify GET record status: Successful =====");

    var id = UUID.randomUUID();
    saveCreationStatus(id, id, metadata, jdbcTemplate);

    var expectedDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+");
    getResultActions(recordsEditorStatusPath(QM_RECORD_ID, id.toString()))
      .andExpect(status().isOk())
      .andExpect(content().string(hasJsonPath("$.metadata.createdAt", matchesPattern(expectedDatePattern))));
  }

  @Test
  void testReturn404IfStatusNotFound() throws Exception {
    log.info("===== Verify GET record status: Not found =====");

    var notExistedId = UUID.randomUUID().toString();

    getResultActions(recordsEditorStatusPath(QM_RECORD_ID, notExistedId))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message").value(containsString("not found")));
  }

  @Test
  void testReturn400IfQmRecordIdIsInvalid() throws Exception {
    log.info("===== Verify GET record status: Parameter invalid =====");

    var invalidId = "invalid";

    getResultActions(recordsEditorStatusPath(QM_RECORD_ID, invalidId))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value(containsString("Parameter 'qmRecordId' is invalid")));
  }

  @Test
  void testReturn400IfQmRecordIdIsMissing() throws Exception {
    log.info("===== Verify GET record status: Parameter missing =====");

    getResultActions(recordsEditorStatusPath())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value(containsString("Parameter 'qmRecordId' is required")));
  }

  @Test
  void testReturn400IfQmRecordIdIsEmpty() throws Exception {
    log.info("===== Verify GET record status: Parameter empty =====");

    getResultActions(recordsEditorStatusPath(QM_RECORD_ID, ""))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value(containsString("Parameter 'qmRecordId' is required")));
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testPostQuickMarcValidBibRecordCreated() throws Exception {
    log.info("===== Verify POST bib record: Successful =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    String jobExecution = "mockdata/change-manager/job-execution/jobExecutionCreated.json";
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, jobExecution, wireMockServer);

    final var updateJobExecutionProfile = String.format(CHANGE_MANAGER_JOB_PROFILE_PATH, VALID_JOB_EXECUTION_ID);
    mockPut(updateJobExecutionProfile, SC_OK, wireMockServer);

    final var postRecordsPath = String.format(CHANGE_MANAGER_PARSE_RECORDS_PATH, VALID_JOB_EXECUTION_ID);
    mockPost(postRecordsPath, "", wireMockServer);
    mockPost(postRecordsPath, "", wireMockServer);

    MvcResult result = postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.status").value(CreationStatus.StatusEnum.NEW.getValue()))
      .andReturn();

    String resultResponse = result.getResponse().getContentAsString();
    CreationStatus response = getObjectFromJson(resultResponse, CreationStatus.class);

    final var qmRecordId = response.getQmRecordId();

    sendDIKafkaRecord("mockdata/di-event/complete-event-with-instance.json", DI_COMPLETE_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> Assertions.assertThat(getCreationStatusById(qmRecordId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.CREATED)
      );
    var creationStatus = getCreationStatusById(qmRecordId, metadata, jdbcTemplate);
    Assertions.assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("errorMessage")
      .hasFieldOrPropertyWithValue("id", qmRecordId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED)
      .hasFieldOrPropertyWithValue("jobExecutionId", VALID_JOB_EXECUTION_ID);
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testPostQuickMarcValidHoldingsRecordCreated() throws Exception {
    log.info("===== Verify POST holdings record: Successful =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_HOLDINGS_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    String jobExecution = "mockdata/change-manager/job-execution/jobExecutionCreated.json";
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, jobExecution, wireMockServer);

    final var updateJobExecutionProfile = String.format(CHANGE_MANAGER_JOB_PROFILE_PATH, VALID_JOB_EXECUTION_ID);
    mockPut(updateJobExecutionProfile, SC_OK, wireMockServer);

    final var postRecordsPath = String.format(CHANGE_MANAGER_PARSE_RECORDS_PATH, VALID_JOB_EXECUTION_ID);
    mockPost(postRecordsPath, "", wireMockServer);
    mockPost(postRecordsPath, "", wireMockServer);

    MvcResult result = postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.status").value(CreationStatus.StatusEnum.NEW.getValue()))
      .andReturn();

    String resultResponse = result.getResponse().getContentAsString();
    CreationStatus response = getObjectFromJson(resultResponse, CreationStatus.class);

    final var qmRecordId = response.getQmRecordId();

    sendDIKafkaRecord("mockdata/di-event/complete-event-with-holdings.json", DI_COMPLETE_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> Assertions.assertThat(getCreationStatusById(qmRecordId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.CREATED)
      );
    var creationStatus = getCreationStatusById(qmRecordId, metadata, jdbcTemplate);
    Assertions.assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("errorMessage")
      .hasFieldOrPropertyWithValue("id", qmRecordId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED)
      .hasFieldOrPropertyWithValue("jobExecutionId", VALID_JOB_EXECUTION_ID);
  }

  @Test
  void testReturn401WhenInvalidUserId() throws Exception {
    log.info("===== Verify POST record: User Id Invalid =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    String jobExecution = "mockdata/change-manager/job-execution/jobExecution_invalid_user_id.json";
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, jobExecution, SC_UNPROCESSABLE_ENTITY, wireMockServer);

    postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
      .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void testReturn401WhenNoHeader() throws Exception {
    log.info("===== Verify POST record: Bad request =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    postResultActions(recordsEditorPath(), quickMarcJson)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("X-Okapi-User-Id header is missing"));
  }

  @Test
  void testReturn400WhenConnectionReset() throws Exception {
    log.info("===== Verify POST record: Connection reset =====");

    QuickMarc quickMarcJson = readQuickMarc(QM_EDITED_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    wireMockServer.stubFor(post(urlEqualTo(CHANGE_MANAGER_JOB_EXECUTION_PATH))
      .willReturn(aResponse()
        .withStatus(SC_OK)
        .withFault(Fault.CONNECTION_RESET_BY_PEER)
      ));

    postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
      .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
      .andExpect(jsonPath("$.message").value(containsString("Connection reset executing")));
  }

  private void checkParseRecordDtoId() {
    var changeManagerResponse = wireMockServer.getAllServeEvents().get(1).getResponse().getBodyAsString();
    ParsedRecordDto parsedRecordDto = getObjectFromJson(changeManagerResponse, ParsedRecordDto.class);
    assertThat(parsedRecordDto.getId(), equalTo(String.valueOf(VALID_PARSED_RECORD_DTO_ID)));
  }
}
