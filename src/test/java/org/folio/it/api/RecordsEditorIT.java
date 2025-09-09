package org.folio.it.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.folio.qm.validation.FieldValidationRule.IS_REQUIRED_TAG_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.folio.support.utils.ApiTestUtils.CHANGE_MANAGER_JOB_EXECUTION_PATH;
import static org.folio.support.utils.ApiTestUtils.JOHN_USER_ID_HEADER;
import static org.folio.support.utils.ApiTestUtils.QM_RECORD_ID;
import static org.folio.support.utils.ApiTestUtils.linksByInstanceIdPath;
import static org.folio.support.utils.ApiTestUtils.mockGet;
import static org.folio.support.utils.ApiTestUtils.recordsEditorPath;
import static org.folio.support.utils.ApiTestUtils.recordsEditorStatusPath;
import static org.folio.support.utils.ApiTestUtils.recordsEditorValidatePath;
import static org.folio.support.utils.ApiTestUtils.sourceStoragePath;
import static org.folio.support.utils.ApiTestUtils.usersByIdPath;
import static org.folio.support.utils.DataBaseTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.support.utils.DataBaseTestUtils.getCreationStatusById;
import static org.folio.support.utils.DataBaseTestUtils.saveCreationStatus;
import static org.folio.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.support.utils.JsonTestUtils.getObjectFromJson;
import static org.folio.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.support.utils.TestEntitiesUtils.AUTHORITY_ID;
import static org.folio.support.utils.TestEntitiesUtils.DI_EVENT_WITH_AUTHORITY;
import static org.folio.support.utils.TestEntitiesUtils.DI_EVENT_WITH_HOLDINGS;
import static org.folio.support.utils.TestEntitiesUtils.DI_EVENT_WITH_INSTANCE;
import static org.folio.support.utils.TestEntitiesUtils.HOLDINGS_ID;
import static org.folio.support.utils.TestEntitiesUtils.INSTANCE_ID;
import static org.folio.support.utils.TestEntitiesUtils.JOB_EXECUTION_ID;
import static org.folio.support.utils.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_CREATE_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_CREATE_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_CREATE_HOLDINGS_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VALIDATE_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VALIDATE_SUBFIELD_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_HOLDINGS_PATH;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.http.Fault;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.BaseIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@Log4j2
@IntegrationTest
class RecordsEditorIT extends BaseIT {

  @Nested
  class GetRecordCases {

    public static Stream<Arguments> getRecordsCasesPositive() {
      return Stream.of(
        Arguments.argumentSet("bibliographic", INSTANCE_ID, QM_RECORD_VIEW_BIB_PATH),
        Arguments.argumentSet("holdings", HOLDINGS_ID, QM_RECORD_VIEW_HOLDINGS_PATH),
        Arguments.argumentSet("authority", AUTHORITY_ID, QM_RECORD_VIEW_AUTHORITY_PATH)
      );
    }

    @ParameterizedTest
    @MethodSource("getRecordsCasesPositive")
    @DisplayName("Should return quickMarc record successfully")
    void testGetQuickMarcRecord(String externalId, String expectedResponsePath) throws Exception {
      var contentAsString = doGet(recordsEditorPath(externalId))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
      var actual = getObjectFromJson(contentAsString, QuickMarcView.class);
      var expected = getMockAsObject(expectedResponsePath, QuickMarcView.class);
      assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should not fail on 404 user")
    void testGetQuickMarcRecord_updatedByUserNotFound() throws Exception {
      mockGet(usersByIdPath(JOHN_USER_ID), null, SC_NOT_FOUND, 1, wireMockServer);

      doGet(recordsEditorPath(HOLDINGS_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.marcFormat").value(MarcFormat.HOLDINGS.getValue()))
        .andExpect(jsonPath("$.parsedRecordDtoId").value(HOLDINGS_ID))
        .andExpect(jsonPath("$.externalId").value(HOLDINGS_ID))
        .andExpect(jsonPath("$.parsedRecordId").value(HOLDINGS_ID))
        .andExpect(jsonPath("$.updateInfo.updatedBy.userId").doesNotExist());
    }

    @Test
    @DisplayName("Should not fail on 404 links")
    void testGetQuickMarcBibRecord_linksNotFound() throws Exception {
      mockGet(linksByInstanceIdPath(INSTANCE_ID), null, SC_NOT_FOUND, 1, wireMockServer);

      doGet(recordsEditorPath(INSTANCE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.marcFormat").value(MarcFormat.BIBLIOGRAPHIC.getValue()))
        .andExpect(jsonPath("$.parsedRecordDtoId").value(INSTANCE_ID))
        .andExpect(jsonPath("$.externalId").value(INSTANCE_ID))
        .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
        .andExpect(jsonPath("$.parsedRecordId").value(INSTANCE_ID))
        .andExpect(jsonPath("$.updateInfo.updatedBy.userId").value(JOHN_USER_ID))
        .andExpect(jsonPath("$.fields[0].authorityId").doesNotExist())
        .andExpect(jsonPath("$.fields[0].authorityNaturalId").doesNotExist())
        .andExpect(jsonPath("$.fields[0].authorityControlledSubfields").doesNotExist());
    }

    @Test
    @DisplayName("Should fail on 404 source records")
    void testGetQuickMarcRecordNotFound() throws Exception {
      var randomId = UUID.randomUUID().toString();
      mockGet(sourceStoragePath(randomId), "Not found", SC_NOT_FOUND, wireMockServer);

      doGet(recordsEditorPath(randomId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()));
    }

    @Test
    @DisplayName("Should fail on convertion exception")
    void testGetQuickMarcRecordConverterError() throws Exception {
      var randomId = UUID.randomUUID().toString();
      mockGet(sourceStoragePath(randomId), "{\"recordType\": \"MARC_BIB\"}", SC_OK, wireMockServer);

      doGet(recordsEditorPath(randomId))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
        .andExpect(jsonPath("$.message")
          .value("org.marc4j.MarcException: Premature end of input in JSON file"));
    }

    @Test
    @DisplayName("Should fail on missing externalId parameter")
    void testGetQuickMarcRecordWithoutInstanceIdParameter() throws Exception {
      doGet(recordsEditorPath("X", UUID.randomUUID().toString()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
        .andExpect(jsonPath("$.message").value("Parameter 'externalId' is required"));
    }
  }

  @Nested
  class ValidateRecordCases {

    @Test
    void testValidateRecord() throws Exception {
      var validatableRecord = readQuickMarc(QM_RECORD_VALIDATE_PATH, ValidatableRecord.class);

      doPost(recordsEditorValidatePath(), validatableRecord)
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
      var validatableRecord = readQuickMarc(QM_RECORD_VALIDATE_SUBFIELD_PATH, ValidatableRecord.class);

      doPost(recordsEditorValidatePath(), validatableRecord)
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
  }

  @Nested
  @DatabaseCleanup(tables = RECORD_CREATION_STATUS_TABLE_NAME)
  class GetCreationStatusCases {

    @Test
    @DisplayName("Should return creation status successfully")
    void testGetCreationStatus() throws Exception {
      var id = UUID.randomUUID().toString();
      var expectedDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+");
      saveCreationStatus(id, id, metadata, jdbcTemplate);

      doGet(recordsEditorStatusPath(QM_RECORD_ID, id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.qmRecordId").value(id))
        .andExpect(jsonPath("$.status").value(CreationStatus.StatusEnum.NEW.getValue()))
        .andExpect(jsonPath("$.metadata").value(notNullValue()))
        .andExpect(jsonPath("$.metadata.createdAt", matchesPattern(expectedDatePattern)));
    }

    @Test
    @DisplayName("Should return 404 if status not found")
    void testReturn404IfStatusNotFound() throws Exception {
      var notExistedId = UUID.randomUUID().toString();

      doGet(recordsEditorStatusPath(QM_RECORD_ID, notExistedId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @DisplayName("Should return 400 if qmRecordId is invalid")
    void testReturn400IfQmRecordIdIsInvalid() throws Exception {
      doGet(recordsEditorStatusPath(QM_RECORD_ID, "invalidId"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("Parameter 'qmRecordId' is invalid")));
    }

    @Test
    @DisplayName("Should return 400 if qmRecordId is missing")
    void testReturn400IfQmRecordIdIsMissing() throws Exception {
      log.info("===== Verify GET record status: Parameter missing =====");

      doGet(recordsEditorStatusPath())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("Parameter 'qmRecordId' is required")));
    }

    @Test
    @DisplayName("Should return 400 if qmRecordId is empty")
    void testReturn400IfQmRecordIdIsEmpty() throws Exception {
      log.info("===== Verify GET record status: Parameter empty =====");

      doGet(recordsEditorStatusPath(QM_RECORD_ID, ""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("Parameter 'qmRecordId' is required")));
    }
  }

  @Nested
  @DatabaseCleanup(tables = RECORD_CREATION_STATUS_TABLE_NAME)
  class CreateRecordCases {

    public static Stream<Arguments> testCreateMarcRecordWithout001FieldCases() {
      return Stream.of(
        Arguments.argumentSet("bibliographic", QM_RECORD_CREATE_BIB_PATH, DI_EVENT_WITH_INSTANCE),
        Arguments.argumentSet("authority", QM_RECORD_CREATE_AUTHORITY_PATH, DI_EVENT_WITH_AUTHORITY)
      );
    }

    static Stream<Arguments> testCreateMarcRecordPositiveCases() {
      return Stream.of(
        Arguments.argumentSet("bibliographic", QM_RECORD_CREATE_BIB_PATH, DI_EVENT_WITH_INSTANCE),
        Arguments.argumentSet("holdings", QM_RECORD_CREATE_HOLDINGS_PATH, DI_EVENT_WITH_HOLDINGS),
        Arguments.argumentSet("authority", QM_RECORD_CREATE_AUTHORITY_PATH, DI_EVENT_WITH_AUTHORITY)
      );
    }

    @ParameterizedTest
    @MethodSource("testCreateMarcRecordPositiveCases")
    @DisplayName("Should create record successfully")
    void testCreateMarcRecordPositive(String requestBody, String eventBody) throws Exception {
      var quickMarcRecord = readQuickMarc(requestBody, QuickMarcCreate.class);

      var result = doPost(recordsEditorPath(), quickMarcRecord)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.jobExecutionId").value(JOB_EXECUTION_ID))
        .andExpect(jsonPath("$.status").value(CreationStatus.StatusEnum.IN_PROGRESS.getValue()))
        .andReturn().getResponse().getContentAsString();

      var qmRecordId = getObjectFromJson(result, CreationStatus.class).getQmRecordId();

      // send DI_COMPLETE topic event
      sendDataImportKafkaRecord(eventBody, DI_COMPLETE_TOPIC_NAME);

      // wait until the status of creation changed to CREATED
      awaitAndAssertStatus(qmRecordId);
    }

    @ParameterizedTest
    @MethodSource("testCreateMarcRecordWithout001FieldCases")
    @DisplayName("Should create record successfully without 001")
    void testCreateMarcRecordWithout001Field(String requestBody, String eventBody) throws Exception {
      var quickMarcRecord = readQuickMarc(requestBody, QuickMarcCreate.class);
      // remove the 001 field from the record and try to create a record
      quickMarcRecord.getFields().removeIf(field -> field.getTag().equals("001"));

      var result = doPost(recordsEditorPath(), quickMarcRecord, JOHN_USER_ID_HEADER)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.jobExecutionId").value(JOB_EXECUTION_ID))
        .andExpect(jsonPath("$.status").value(CreationStatus.StatusEnum.IN_PROGRESS.getValue()))
        .andReturn().getResponse().getContentAsString();

      var qmRecordId = getObjectFromJson(result, CreationStatus.class).getQmRecordId();

      // send DI_COMPLETE topic event
      sendDataImportKafkaRecord(eventBody, DI_COMPLETE_TOPIC_NAME);

      // wait until the status of creation changed to CREATED
      awaitAndAssertStatus(qmRecordId);
    }

    @Test
    @DisplayName("Should return 422 if user id is invalid")
    void testReturn401WhenInvalidUserId() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_CREATE_BIB_PATH, QuickMarcCreate.class);

      doPost(recordsEditorPath(), quickMarcRecord, Map.of("x-okapi-custom", "invalid-id"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
        .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    @DisplayName("Should return 422 if multiply 852 fields in holdings record")
    void testReturn422WhenCreateHoldingsWithMultiply852() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_CREATE_HOLDINGS_PATH, QuickMarcCreate.class);
      quickMarcRecord.getFields().add(new FieldItem().tag("852").content("$b content"));

      doPost(recordsEditorPath(), quickMarcRecord, JOHN_USER_ID_HEADER)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
        .andExpect(jsonPath("$.message").value(IS_UNIQUE_TAG_ERROR_MSG));
    }

    @Test
    @DisplayName("Should return 422 if multiply 001 fields in holdings record")
    void testReturn422WhenHoldingsRecordWithMultiple001() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_CREATE_HOLDINGS_PATH, QuickMarcCreate.class);
      quickMarcRecord.getFields().add(new FieldItem().tag("001").content("$a test content"));

      doPost(recordsEditorPath(), quickMarcRecord, JOHN_USER_ID_HEADER)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
        .andExpect(jsonPath("$.message").value(IS_UNIQUE_TAG_ERROR_MSG));
    }

    @Test
    @DisplayName("Should return 422 if specification-based validation failed (multiple 001)")
    void testReturn422WhenRecordWithMultiple001() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_CREATE_BIB_PATH, QuickMarcCreate.class);
      quickMarcRecord.getFields().add(new FieldItem().tag("001").content("$a test content"));

      doPost(recordsEditorPath(), quickMarcRecord, JOHN_USER_ID_HEADER)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.issues.size()").value(1))
        .andExpect(jsonPath("$.issues[0].tag").value("001[1]"))
        .andExpect(jsonPath("$.issues[0].helpUrl").value("https://www.loc.gov/marc/bibliographic/bd001.html"))
        .andExpect(jsonPath("$.issues[0].severity").value("error"))
        .andExpect(jsonPath("$.issues[0].definitionType").value("field"))
        .andExpect(jsonPath("$.issues[0].message").value("Field is non-repeatable."));
    }

    @ParameterizedTest
    @ValueSource(strings = {QM_RECORD_CREATE_BIB_PATH, QM_RECORD_CREATE_HOLDINGS_PATH})
    @DisplayName("Should return 422 if specification-based validation failed (missing 008)")
    void testReturn422WhenRecordMissing008(String filePath) throws Exception {
      var quickMarcRecord = readQuickMarc(filePath, QuickMarcCreate.class);
      quickMarcRecord.getFields().removeIf(field -> field.getTag().equals("008"));

      doPost(recordsEditorPath(), quickMarcRecord, JOHN_USER_ID_HEADER)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
        .andExpect(jsonPath("$.message").value(IS_REQUIRED_TAG_ERROR_MSG));
    }

    @Test
    @DisplayName("Should return 400 if connection reset")
    void testReturn400WhenConnectionReset() throws Exception {
      wireMockServer.stubFor(post(urlEqualTo(CHANGE_MANAGER_JOB_EXECUTION_PATH))
        .atPriority(1)
        .willReturn(aResponse()
          .withStatus(SC_OK)
          .withFault(Fault.CONNECTION_RESET_BY_PEER)
        ));

      QuickMarcCreate quickMarcJson = readQuickMarc(QM_RECORD_CREATE_BIB_PATH, QuickMarcCreate.class);

      doPost(recordsEditorPath(), quickMarcJson)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value(containsString("Connection reset executing")));
    }

    private void awaitAndAssertStatus(UUID qmRecordId) {
      await().atMost(5, SECONDS)
        .untilAsserted(() -> assertThat(getCreationStatusById(qmRecordId, metadata, jdbcTemplate).getStatus())
          .isEqualTo(RecordCreationStatusEnum.CREATED));
      var creationStatus = getCreationStatusById(qmRecordId, metadata, jdbcTemplate);
      assertThat(creationStatus)
        .hasNoNullFieldsOrPropertiesExcept("errorMessage")
        .hasFieldOrPropertyWithValue("id", qmRecordId)
        .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED)
        .hasFieldOrPropertyWithValue("jobExecutionId", UUID.fromString(JOB_EXECUTION_ID));
    }
  }
}
