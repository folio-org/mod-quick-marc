package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.awaitility.Awaitility.await;
import static org.folio.qm.support.utils.ApiTestUtils.CHANGE_MANAGER_JOB_EXECUTION_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.CHANGE_MANAGER_JOB_PROFILE_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.CHANGE_MANAGER_PARSE_RECORDS_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.EXTERNAL_ID;
import static org.folio.qm.support.utils.ApiTestUtils.FIELD_PROTECTION_SETTINGS_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.JOHN_USER_ID_HEADER;
import static org.folio.qm.support.utils.ApiTestUtils.LINKING_RULES_FETCHING_PATH;
import static org.folio.qm.support.utils.ApiTestUtils.QM_RECORD_ID;
import static org.folio.qm.support.utils.ApiTestUtils.TENANT_ID;
import static org.folio.qm.support.utils.ApiTestUtils.changeManagerPath;
import static org.folio.qm.support.utils.ApiTestUtils.linksByInstanceIdPath;
import static org.folio.qm.support.utils.ApiTestUtils.mockGet;
import static org.folio.qm.support.utils.ApiTestUtils.mockPost;
import static org.folio.qm.support.utils.ApiTestUtils.mockPut;
import static org.folio.qm.support.utils.ApiTestUtils.recordsEditorPath;
import static org.folio.qm.support.utils.ApiTestUtils.recordsEditorStatusPath;
import static org.folio.qm.support.utils.ApiTestUtils.usersByIdPath;
import static org.folio.qm.support.utils.DataBaseTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.support.utils.DataBaseTestUtils.getCreationStatusById;
import static org.folio.qm.support.utils.DataBaseTestUtils.saveCreationStatus;
import static org.folio.qm.support.utils.InputOutputTestUtils.readFile;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectFromJson;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.AUTHORITY_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.AUTHORITY_NATURAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_HRID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOB_EXECUTION_CREATED;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.LINKING_RULE_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.LINK_ERROR_CAUSE;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.LINK_STATUS_ERROR;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_CREATE_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_CREATE_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_CREATE_HOLDINGS_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_VALIDATE_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_VALIDATE_SUBFIELD_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.USER_JOHN_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;
import static org.folio.qm.validation.FieldValidationRule.IS_REQUIRED_TAG_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.http.Fault;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.support.utils.testentities.TestEntitiesUtils;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Log4j2
@IntegrationTest
class RecordsEditorIT extends BaseIT {

  @Test
  void testGetQuickMarcBibRecord() throws Exception {
    log.info("===== Verify GET Bibliographic record: Successful =====");

    mockGet(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), readFile(TestEntitiesUtils.LINKS_PATH), SC_OK, wireMockServer);
    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_BIB_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, wireMockServer);
    mockGet(FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      wireMockServer);
    mockGet(LINKING_RULES_FETCHING_PATH, readFile(TestEntitiesUtils.LINKING_RULES_PATH), SC_OK, wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.marcFormat").value(MarcFormat.BIBLIOGRAPHIC.getValue()))
      .andExpect(jsonPath("$.parsedRecordDtoId").value(VALID_PARSED_RECORD_DTO_ID.toString()))
      .andExpect(jsonPath("$.externalId").value(EXISTED_EXTERNAL_ID.toString()))
      .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
      .andExpect(jsonPath("$.parsedRecordId").value(VALID_PARSED_RECORD_ID.toString()))
      .andExpect(jsonPath("$.updateInfo.updatedBy.userId").value(JOHN_USER_ID))
      .andExpect(jsonPath("$.fields[14].linkDetails.authorityId").value(AUTHORITY_ID))
      .andExpect(jsonPath("$.fields[14].linkDetails.authorityNaturalId").value(AUTHORITY_NATURAL_ID))
      .andExpect(jsonPath("$.fields[14].linkDetails.linkingRuleId").value(LINKING_RULE_ID))
      .andExpect(jsonPath("$.fields[14].linkDetails.status").value(LINK_STATUS_ERROR))
      .andExpect(jsonPath("$.fields[14].linkDetails.errorCause").value(LINK_ERROR_CAUSE));

    checkParseRecordDtoId();
  }

  @Test
  void testValidateRecord() throws Exception {
    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    var validatableRecord = readQuickMarc(QM_RECORD_VALIDATE_PATH, ValidatableRecord.class);

    postResultActions("/records-editor/validate", validatableRecord, Map.of())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.issues.size()").value(2))
      .andExpect(jsonPath("$.issues[0].tag").value("246[0]"))
      .andExpect(jsonPath("$.issues[0].helpUrl").value("https://www.loc.gov/marc/bibliographic/bd246.html"))
      .andExpect(jsonPath("$.issues[0].severity").value("error"))
      .andExpect(jsonPath("$.issues[0].definitionType").value("field"))
      .andExpect(jsonPath("$.issues[0].message").value("Field 246 is required."))

      .andExpect(jsonPath("$.issues[1].tag").value("245[1]"))
      .andExpect(jsonPath("$.issues[1].helpUrl").value("https://www.loc.gov/marc/bibliographic/bd245.html"))
      .andExpect(jsonPath("$.issues[1].severity").value("error"))
      .andExpect(jsonPath("$.issues[1].definitionType").value("field"))
      .andExpect(jsonPath("$.issues[1].message").value("Field is non-repeatable."));
  }

  @Test
  void testValidateRecordWithNonRepeatableSubfieldValidationError() throws Exception {
    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    var validatableRecord = readQuickMarc(QM_RECORD_VALIDATE_SUBFIELD_PATH, ValidatableRecord.class);

    postResultActions("/records-editor/validate", validatableRecord, Map.of())
      .andExpect(status().isOk())

      .andExpect(jsonPath("$.issues[0].tag").value("245[0]"))
      .andExpect(jsonPath("$.issues[0].helpUrl").value("https://www.loc.gov/marc/bibliographic/bd245.html"))
      .andExpect(jsonPath("$.issues[0].severity").value("error"))
      .andExpect(jsonPath("$.issues[0].definitionType").value("subfield"))
      .andExpect(jsonPath("$.issues[0].message").value("Subfield '6' is non-repeatable."))

      .andExpect(jsonPath("$.issues[1].tag").value("245[1]"))
      .andExpect(jsonPath("$.issues[1].helpUrl").value("https://www.loc.gov/marc/bibliographic/bd245.html"))
      .andExpect(jsonPath("$.issues[1].severity").value("error"))
      .andExpect(jsonPath("$.issues[1].definitionType").value("field"))
      .andExpect(jsonPath("$.issues[1].message").value("Field is non-repeatable."))

      .andExpect(jsonPath("$.issues[2].tag").value("245[2]"))
      .andExpect(jsonPath("$.issues[2].helpUrl").value("https://www.loc.gov/marc/bibliographic/bd245.html"))
      .andExpect(jsonPath("$.issues[2].severity").value("error"))
      .andExpect(jsonPath("$.issues[2].definitionType").value("field"))
      .andExpect(jsonPath("$.issues[2].message").value("Field is non-repeatable."))

      .andExpect(jsonPath("$.issues[3].tag").value("245[2]"))
      .andExpect(jsonPath("$.issues[3].helpUrl").value("https://www.loc.gov/marc/bibliographic/bd245.html"))
      .andExpect(jsonPath("$.issues[3].severity").value("error"))
      .andExpect(jsonPath("$.issues[3].definitionType").value("subfield"))
      .andExpect(jsonPath("$.issues[3].message").value("Subfield 'a' is non-repeatable."));
  }

  @Test
  void testGetQuickMarcHoldingsRecord() throws Exception {
    log.info("===== Verify GET Holdings record: Successful =====");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_HOLDINGS_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, wireMockServer);
    mockGet(FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      wireMockServer);

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
  void testGetQuickMarcHoldingsRecord_updatedByUserNotFound() throws Exception {
    log.info("===== Verify GET Holdings record: user is not found =====");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_HOLDINGS_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), null, SC_NOT_FOUND, wireMockServer);
    mockGet(FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.marcFormat").value(MarcFormat.HOLDINGS.getValue()))
      .andExpect(jsonPath("$.parsedRecordDtoId").value(VALID_PARSED_RECORD_DTO_ID.toString()))
      .andExpect(jsonPath("$.externalId").value(EXISTED_EXTERNAL_ID.toString()))
      .andExpect(jsonPath("$.externalHrid").value(EXISTED_EXTERNAL_HRID))
      .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
      .andExpect(jsonPath("$.parsedRecordId").value(VALID_PARSED_RECORD_ID.toString()))
      .andExpect(jsonPath("$.updateInfo.updatedBy.userId").doesNotExist());

    checkParseRecordDtoId();
  }

  @Test
  void testGetQuickMarcBibRecord_linksNotFound() throws Exception {
    log.info("===== Verify GET Bibliographic record: Successful =====");

    mockGet(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), null, SC_NOT_FOUND, wireMockServer);
    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_BIB_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, wireMockServer);
    mockGet(FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      wireMockServer);
    mockGet(LINKING_RULES_FETCHING_PATH, readFile(TestEntitiesUtils.LINKING_RULES_PATH), SC_OK, wireMockServer);

    getResultActions(recordsEditorPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID))
      .andDo(result -> log.info("KEK" + result.getResponse().getContentAsString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.marcFormat").value(MarcFormat.BIBLIOGRAPHIC.getValue()))
      .andExpect(jsonPath("$.parsedRecordDtoId").value(VALID_PARSED_RECORD_DTO_ID.toString()))
      .andExpect(jsonPath("$.externalId").value(EXISTED_EXTERNAL_ID.toString()))
      .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
      .andExpect(jsonPath("$.parsedRecordId").value(VALID_PARSED_RECORD_ID.toString()))
      .andExpect(jsonPath("$.updateInfo.updatedBy.userId").value(JOHN_USER_ID))
      .andExpect(jsonPath("$.fields[0].authorityId").doesNotExist())
      .andExpect(jsonPath("$.fields[0].authorityNaturalId").doesNotExist())
      .andExpect(jsonPath("$.fields[0].authorityControlledSubfields").doesNotExist());

    checkParseRecordDtoId();
  }

  @Test
  void testGetQuickMarcAuthorityRecord() throws Exception {
    log.info("===== Verify GET Authority record: Successful =====");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_AUTHORITY_DTO_PATH), SC_OK,
      wireMockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, wireMockServer);
    mockGet(FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      wireMockServer);

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
    mockGet(FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      wireMockServer);

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
  @DatabaseCleanup(tables = RECORD_CREATION_STATUS_TABLE_NAME)
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
  @DatabaseCleanup(tables = RECORD_CREATION_STATUS_TABLE_NAME)
  void testGetCreationStatusHasProperlyFormattedDate() throws Exception {
    log.info("===== Verify GET record status: Successful =====");

    var id = UUID.randomUUID();
    saveCreationStatus(id, id, metadata, jdbcTemplate);

    var expectedDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+");
    getResultActions(recordsEditorStatusPath(QM_RECORD_ID, id.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.metadata.createdAt", matchesPattern(expectedDatePattern)));
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

  @CsvSource(value = {
    QM_RECORD_CREATE_HOLDINGS_PATH + ", " + "mockdata/request/di-event/complete-event-with-holdings.json",
    QM_RECORD_CREATE_AUTHORITY_PATH + ", " + "mockdata/request/di-event/complete-event-with-authority.json",
    QM_RECORD_CREATE_BIB_PATH + ", " + "mockdata/request/di-event/complete-event-with-instance.json"
  })
  @ParameterizedTest
  @DatabaseCleanup(tables = RECORD_CREATION_STATUS_TABLE_NAME)
  void testPostQuickMarcValidRecordCreated(String requestBody, String eventBody) throws Exception {
    log.info("===== Verify POST record: Successful =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=authority",
      readFile("mockdata/response/specifications/specificationAuthority.json"), SC_OK, wireMockServer);
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, JOB_EXECUTION_CREATED, wireMockServer);

    final var updateJobExecutionProfile = String.format(CHANGE_MANAGER_JOB_PROFILE_PATH, VALID_JOB_EXECUTION_ID);
    mockPut(updateJobExecutionProfile, SC_OK, wireMockServer);

    final var postRecordsPath = String.format(CHANGE_MANAGER_PARSE_RECORDS_PATH, VALID_JOB_EXECUTION_ID);
    mockPost(postRecordsPath, "", wireMockServer);
    mockPost(postRecordsPath, "", wireMockServer);

    QuickMarcCreate quickMarcJson = readQuickMarc(requestBody, QuickMarcCreate.class);

    MvcResult result = postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.status").value(CreationStatus.StatusEnum.IN_PROGRESS.getValue()))
      .andReturn();

    String resultResponse = result.getResponse().getContentAsString();
    CreationStatus response = getObjectFromJson(resultResponse, CreationStatus.class);

    final var qmRecordId = response.getQmRecordId();

    sendDataImportKafkaRecord(eventBody, DI_COMPLETE_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> Assertions.assertThat(getCreationStatusById(qmRecordId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.CREATED));
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

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    String jobExecution = "mockdata/request/change-manager/job-execution/jobExecution_invalid_user_id.json";
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, jobExecution, SC_UNPROCESSABLE_ENTITY, wireMockServer);

    QuickMarcCreate quickMarcJson = readQuickMarc(QM_RECORD_CREATE_BIB_PATH, QuickMarcCreate.class);

    postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
      .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void testReturn400WhenNoHeader() throws Exception {
    log.info("===== Verify POST record: Bad request =====");

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    mockMvc.perform(MockMvcRequestBuilders.post(recordsEditorPath())
        .header(XOkapiHeaders.TENANT, TENANT_ID)
        .content(getObjectAsJson(quickMarcJson)))
      .andDo(log())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("x-okapi-user-id header must be provided")));
  }

  @Test
  void testReturn422WhenCreateHoldingsWithMultiply852() throws Exception {
    log.info("===== Verify POST record: Multiply 852 =====");

    QuickMarcCreate quickMarcJson = readQuickMarc(QM_RECORD_CREATE_HOLDINGS_PATH, QuickMarcCreate.class);

    quickMarcJson.getFields().add(new FieldItem().tag("852").content("$b content"));

    postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(jsonPath("$.message").value(IS_UNIQUE_TAG_ERROR_MSG));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_CREATE_BIB_PATH, QM_RECORD_CREATE_HOLDINGS_PATH})
  void testReturn422WhenRecordWithMultiple001(String filePath) throws Exception {
    log.info("===== Verify POST record: Multiple 001 =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    QuickMarcCreate quickMarcJson = readQuickMarc(filePath, QuickMarcCreate.class);

    quickMarcJson.getFields().add(new FieldItem().tag("001").content("$a test content"));

    postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(jsonPath("$.message").value(IS_UNIQUE_TAG_ERROR_MSG));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_CREATE_BIB_PATH, QM_RECORD_CREATE_HOLDINGS_PATH})
  void testReturn422WhenRecordMissing008(String filePath) throws Exception {
    log.info("===== Verify POST record: Missing 008 =====");
    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);
    QuickMarcCreate quickMarcJson = readQuickMarc(filePath, QuickMarcCreate.class);

    quickMarcJson.getFields().removeIf(field -> field.getTag().equals("008"));

    postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(jsonPath("$.message").value(IS_REQUIRED_TAG_ERROR_MSG));
  }

  @Test
  void testReturn400WhenConnectionReset() throws Exception {
    log.info("===== Verify POST record: Connection reset =====");
    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);
    wireMockServer.stubFor(post(urlEqualTo(CHANGE_MANAGER_JOB_EXECUTION_PATH))
      .willReturn(aResponse()
        .withStatus(SC_OK)
        .withFault(Fault.CONNECTION_RESET_BY_PEER)
      ));

    QuickMarcCreate quickMarcJson = readQuickMarc(QM_RECORD_CREATE_BIB_PATH, QuickMarcCreate.class);

    postResultActions(recordsEditorPath(), quickMarcJson, JOHN_USER_ID_HEADER)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
      .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
      .andExpect(jsonPath("$.message").value(containsString("Connection reset executing")));
  }

  private void checkParseRecordDtoId() {
    var serveEvents = wireMockServer.getAllServeEvents();
    var changeManagerResponse = serveEvents.get(serveEvents.size() - 1).getResponse().getBodyAsString();
    ParsedRecordDto parsedRecordDto = getObjectFromJson(changeManagerResponse, ParsedRecordDto.class);
    assertThat(parsedRecordDto.getId(), equalTo(VALID_PARSED_RECORD_DTO_ID));
  }
}
