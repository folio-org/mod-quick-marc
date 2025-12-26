package org.folio.it.api;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.UUID.fromString;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.qm.service.validation.FieldValidationRule.IS_REQUIRED_TAG_ERROR_MSG;
import static org.folio.qm.service.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.folio.support.utils.ApiTestUtils.JOHN_USER_ID_HEADER;
import static org.folio.support.utils.ApiTestUtils.linksByInstanceIdPath;
import static org.folio.support.utils.ApiTestUtils.mockGet;
import static org.folio.support.utils.ApiTestUtils.recordsEditorByIdPath;
import static org.folio.support.utils.ApiTestUtils.recordsEditorPath;
import static org.folio.support.utils.ApiTestUtils.recordsEditorValidatePath;
import static org.folio.support.utils.ApiTestUtils.sourceStoragePath;
import static org.folio.support.utils.ApiTestUtils.usersByIdPath;
import static org.folio.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.support.utils.JsonTestUtils.getObjectFromJson;
import static org.folio.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.support.utils.TestEntitiesUtils.AUTHORITY_ID;
import static org.folio.support.utils.TestEntitiesUtils.HOLDINGS_ID;
import static org.folio.support.utils.TestEntitiesUtils.INSTANCE_ID;
import static org.folio.support.utils.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_CREATE_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_CREATE_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_CREATE_HOLDINGS_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_HOLDINGS_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VALIDATE_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VALIDATE_SUBFIELD_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_HOLDINGS_PATH;
import static org.folio.support.utils.TestEntitiesUtils.getFieldWithValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.folio.it.BaseIT;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.testing.type.IntegrationTest;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.ReflectionUtils;

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
  class CreateRecordCases {

    @ParameterizedTest
    @MethodSource("testCreateMarcRecordPositiveCases")
    @DisplayName("Should create record successfully")
    void testCreateMarcRecordPositive(String requestBody) throws Exception {
      var quickMarcRecord = readQuickMarc(requestBody, QuickMarcCreate.class);

      doPost(recordsEditorPath(), quickMarcRecord)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.externalId").isNotEmpty())
        .andExpect(jsonPath("$.externalHrid").isNotEmpty());
    }

    @ParameterizedTest
    @MethodSource("testCreateMarcRecordWithout001FieldCases")
    @DisplayName("Should create record successfully without 001")
    void testCreateMarcRecordWithout001Field(String requestBody) throws Exception {
      var quickMarcRecord = readQuickMarc(requestBody, QuickMarcCreate.class);
      // remove the 001 field from the record and try to create a record
      quickMarcRecord.getFields().removeIf(field -> field.getTag().equals("001"));

      doPost(recordsEditorPath(), quickMarcRecord, JOHN_USER_ID_HEADER)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.externalId").isNotEmpty())
        .andExpect(jsonPath("$.externalHrid").isNotEmpty());
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

    private static Stream<Arguments> testCreateMarcRecordWithout001FieldCases() {
      return Stream.of(
        Arguments.argumentSet("bibliographic", QM_RECORD_CREATE_BIB_PATH),
        Arguments.argumentSet("authority", QM_RECORD_CREATE_AUTHORITY_PATH)
      );
    }

    private static Stream<Arguments> testCreateMarcRecordPositiveCases() {
      return Stream.of(
        Arguments.argumentSet("bibliographic", QM_RECORD_CREATE_BIB_PATH),
        Arguments.argumentSet("holdings", QM_RECORD_CREATE_HOLDINGS_PATH),
        Arguments.argumentSet("authority", QM_RECORD_CREATE_AUTHORITY_PATH)
      );
    }
  }

  @Nested
  class UpdateRecordCases {
    @ParameterizedTest
    @MethodSource("testUpdateQuickMarcRecordCases")
    @DisplayName("Should update QuickMarc record successfully")
    void testUpdateQuickMarcRecord(String filePath, String externalId) throws Exception {
      var quickMarcRecord = readQuickMarc(filePath, QuickMarcEdit.class);

      doPut(recordsEditorByIdPath(externalId), quickMarcRecord)
        .andDo(log())
        .andExpect(status().isAccepted());

      if (quickMarcRecord.getMarcFormat() == MarcFormat.BIBLIOGRAPHIC) {
        expectLinksUpdateRequests(1, linksByInstanceIdPath(externalId));
      } else {
        expectLinksUpdateRequests(0, linksByInstanceIdPath(externalId));
      }
    }

    @Test
    @DisplayName("Should fail with optimistic locking when source version doesn't match")
    void testUpdateQuickMarcRecordFailedWithOptimisticLocking() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
        .sourceVersion(8);

      doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
        .andExpect(status().isConflict())
        .andExpect(optimisticLockingMessage(INSTANCE_ID, 1, 8));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
    }

    @Test
    @DisplayName("Should fail with 404 when source record doesn't exist")
    void testUpdateQuickMarcRecordWrongUuid() throws Exception {
      log.info("===== Verify PUT record: Not found =====");
      var wrongUuid = UUID.randomUUID().toString();

      mockGet(sourceStoragePath(wrongUuid), "{}", SC_NOT_FOUND, wireMockServer);

      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
        .parsedRecordId(UUID.fromString(wrongUuid))
        .parsedRecordDtoId(UUID.fromString(wrongUuid))
        .externalId(UUID.fromString(wrongUuid));

      doPut(recordsEditorByIdPath(wrongUuid), quickMarcRecord)
        .andExpect(status().isNotFound());

      expectLinksUpdateRequests(0, linksByInstanceIdPath(wrongUuid));
    }

    @Test
    @DisplayName("Should fail with 400 when path Id and externalDtoId are not equal")
    void testUpdateQuickMarcRecordIdsNotEqual() throws Exception {
      var id = fromString(INSTANCE_ID);
      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
        .parsedRecordDtoId(id)
        .externalId(id);

      var pathId = UUID.randomUUID().toString();
      doPut(recordsEditorByIdPath(pathId), quickMarcRecord)
        .andExpect(status().isBadRequest())
        .andExpect(errorMessageMatch(equalTo("Request id and entity id are not equal")));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
      expectLinksUpdateRequests(0, linksByInstanceIdPath(pathId));
    }

    @Test
    @DisplayName("Should fail with 400 when MARC tag has alphabetic symbols")
    void testUpdateQuickMarcRecordTagIsInvalid() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);
      quickMarcRecord.getFields().getFirst().setTag("001-invalid");

      doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
        .andExpect(status().isBadRequest())
        .andExpect(errorMessageMatch(equalTo("Parameter 'fields[0].tag' must match \"^[0-9]{3}$\"")));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
    }

    @Test
    @DisplayName("Should fail with 400 when empty request body")
    void testUpdateQuickMarcRecordWithEmptyBody() throws Exception {
      doPut(recordsEditorByIdPath(INSTANCE_ID))
        .andExpect(status().isBadRequest())
        .andExpect(errorMessageMatch(containsString("Required request body is missing")));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
    }

    @Test
    @DisplayName("Should fail with 400 when MARC indicators are invalid")
    void testUpdateQuickMarcRecordInvalidBody() throws Exception {
      var quickMarcRecord = prepareRecordWithInvalidIndicators();

      doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.size()").value(2))
        .andExpect(jsonPath("$.errors[0].message").value("Should have exactly 2 indicators"))
        .andExpect(jsonPath("$.errors[0].type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
        .andExpect(jsonPath("$.errors[0].parameters[0].key").value("333"))

        .andExpect(jsonPath("$.errors[1].message").value("Is required tag"))
        .andExpect(jsonPath("$.errors[1].type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
        .andExpect(jsonPath("$.errors[1].parameters[0].key").value("008"));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
    }

    @Test
    @DisplayName("Should fail with 400 when invalid fixed length field items")
    void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);

      quickMarcRecord.getFields().stream()
        .filter(fieldItem -> fieldItem.getTag().equals("008"))
        .forEach(fieldItem -> {
          @SuppressWarnings("unchecked")
          var content = (Map<String, Object>) fieldItem.getContent();
          content.put("Date1", "12345");
        });

      doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(errorMessageMatch(equalTo("Invalid Date1 field length, must be 4 characters")));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
    }

    @Test
    @DisplayName("Should update record when leader and ignore 008 Elvl mismatch")
    void testUpdateQuickMarcRecordIgnoreElvlLeaderMismatch() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);

      quickMarcRecord.getFields().stream()
        .filter(fieldItem -> fieldItem.getTag().equals("008"))
        .forEach(fieldItem -> {
          @SuppressWarnings("unchecked")
          var content = (Map<String, Object>) fieldItem.getContent();
          content.put("Elvl", "a");
        });

      doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
        .andExpect(status().isAccepted());
    }

    @ParameterizedTest
    @MethodSource("testUpdateQuickMarcValidationCases")
    @DisplayName("Should fail with 422 when multiple 001 tags")
    void testUpdateReturn422WhenRecordWithMultiple001(String filePath, String id) throws Exception {
      var quickMarcRecord = readQuickMarc(filePath, QuickMarcEdit.class);
      // Now we add the new 001 field to the record and try to update existing record
      quickMarcRecord.getFields().add(new FieldItem().tag("001").content("$a test value"));

      doPut(recordsEditorByIdPath(id), quickMarcRecord)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.issues.size()").value(1))
        .andExpect(jsonPath("$.issues[0].tag").value("001[1]"))
        .andExpect(jsonPath("$.issues[0].severity").value("error"))
        .andExpect(jsonPath("$.issues[0].definitionType").value("field"))
        .andExpect(jsonPath("$.issues[0].message").value("Field is non-repeatable."));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(id));
    }

    @ParameterizedTest
    @MethodSource("testUpdateQuickMarcValidationCases")
    @DisplayName("Should fail with 422 when missing 001 tag")
    void testUpdateReturn422WhenRecordWithout001Field(String filePath, String id) throws Exception {
      var quickMarcRecord = readQuickMarc(filePath, QuickMarcEdit.class);
      // remove the 001 field from the record and try to update existing record
      quickMarcRecord.getFields().removeIf(field -> field.getTag().equals("001"));

      doPut(recordsEditorByIdPath(id), quickMarcRecord)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.issues.size()").value(1))
        .andExpect(jsonPath("$.issues[0].tag").value("001[0]"))
        .andExpect(jsonPath("$.issues[0].severity").value("error"))
        .andExpect(jsonPath("$.issues[0].definitionType").value("field"))
        .andExpect(jsonPath("$.issues[0].message").value("Field 001 is required."));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(id));
    }

    @Test
    @DisplayName("Should fail with 422 when holdings has multiple 001 tags")
    void testUpdateReturn422WhenHoldingsRecordWithMultiple001() throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_HOLDINGS_PATH, QuickMarcEdit.class);
      // Now we add the new 001 field to the record and try to update existing record
      quickMarcRecord.getFields().add(new FieldItem().tag("001").content("$a test value"));

      doPut(recordsEditorByIdPath(HOLDINGS_ID), quickMarcRecord)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(errorMessageMatch(equalTo(IS_UNIQUE_TAG_ERROR_MSG)));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(HOLDINGS_ID));
    }

    @ParameterizedTest
    @MethodSource("testUpdateQuickMarcRecordCases")
    @DisplayName("Should fail with 422 when missing 008 tag")
    void testUpdateReturn422WhenRecordMissed008(String filePath, String id) throws Exception {
      var quickMarcRecord = readQuickMarc(filePath, QuickMarcEdit.class);
      // Now we remove the 008 field from the record and try to update existing record
      quickMarcRecord.getFields().removeIf(field -> field.getTag().equals("008"));

      doPut(recordsEditorByIdPath(id), quickMarcRecord)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(errorMessageMatch(equalTo(IS_REQUIRED_TAG_ERROR_MSG)));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(id));
    }

    @ParameterizedTest
    @DisplayName("Should fail with 400 when record misses required field")
    @ValueSource(strings = {"externalHrid", "externalId", "parsedRecordDtoId", "parsedRecordId"})
    void testUpdateReturn400WhenRecordDoNotHaveRequiredFields(String fieldName) throws Exception {
      var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);
      // set field to null
      Arrays.stream(ReflectionUtils.getAllDeclaredMethods(QuickMarcEdit.class))
        .filter(method -> method.getName().equals(fieldName))
        .findFirst()
        .ifPresent(method -> ReflectionUtils.invokeMethod(method, quickMarcRecord, (Object) null));

      doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
        .andExpect(status().isBadRequest())
        .andExpect(errorMessageMatch(equalTo(String.format("Parameter '%s' must not be null", fieldName))));

      expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
    }

    private QuickMarcEdit prepareRecordWithInvalidIndicators() {
      var field333 = getFieldWithValue("333", "$333 content").content("$333 content")
        .indicators(Collections.singletonList(" "));
      var field245 = getFieldWithValue("245", "$a content");
      var field246 = getFieldWithValue("246", "$a content");
      var field001 = getFieldWithValue("001", "$a content");

      var id = fromString(INSTANCE_ID);
      return new QuickMarcEdit()
        .leader("01542ccm a22002533  4500")
        .fields(Arrays.asList(field333, field333, field245, field246, field001))
        .parsedRecordId(UUID.randomUUID())
        .parsedRecordDtoId(UUID.randomUUID())
        .externalId(UUID.randomUUID())
        .externalHrid("hr0001")
        .sourceVersion(1)
        .parsedRecordDtoId(id)
        .marcFormat(MarcFormat.BIBLIOGRAPHIC)
        .parsedRecordId(id)
        .externalId(id);
    }

    private static Stream<Arguments> testUpdateQuickMarcRecordCases() {
      return Stream.of(
        Arguments.argumentSet("bibliographic", QM_RECORD_EDIT_BIB_PATH, INSTANCE_ID),
        Arguments.argumentSet("holdings", QM_RECORD_EDIT_HOLDINGS_PATH, HOLDINGS_ID),
        Arguments.argumentSet("authority", QM_RECORD_EDIT_AUTHORITY_PATH, AUTHORITY_ID)
      );
    }

    private static Stream<Arguments> testUpdateQuickMarcValidationCases() {
      return Stream.of(
        Arguments.argumentSet("bibliographic", QM_RECORD_EDIT_BIB_PATH, INSTANCE_ID),
        Arguments.argumentSet("authority", QM_RECORD_EDIT_AUTHORITY_PATH, AUTHORITY_ID)
      );
    }

    private void expectLinksUpdateRequests(int expected, String url) {
      wireMockServer.verify(exactly(expected), putRequestedFor(urlEqualTo(url)));
    }

    private ResultMatcher errorMessageMatch(Matcher<String> errorMessageMatcher) {
      return jsonPath("$.message", errorMessageMatcher);
    }

    private ResultMatcher optimisticLockingMessage(String recordId, int storedVersion, int requestVersion) {
      return jsonPath("$.message", equalTo(olMessage(recordId, storedVersion, requestVersion)));
    }

    private String olMessage(String recordId, int storedVersion, int requestVersion) {
      return ("Cannot update record %s because it has been changed "
              + "(optimistic locking): Stored _version is %s, _version of request is %s")
        .formatted(recordId, storedVersion, requestVersion);
    }
  }
}
