package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.qm.support.utils.ApiTestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.support.utils.ApiTestUtils.linksByInstanceIdPath;
import static org.folio.qm.support.utils.ApiTestUtils.mockGet;
import static org.folio.qm.support.utils.ApiTestUtils.mockPut;
import static org.folio.qm.support.utils.ApiTestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.support.utils.InputOutputTestUtils.readFile;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_HOLDINGS_PATH;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.testing.type.IntegrationTest;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.ReflectionUtils;

@Log4j2
@IntegrationTest
class RecordsEditorAsyncIT extends BaseIT {

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_EDIT_BIB_PATH, QM_RECORD_EDIT_HOLDINGS_PATH, QM_RECORD_EDIT_AUTHORITY_PATH})
  void testUpdateQuickMarcRecord(String filePath) throws Exception {
    log.info("===== Verify PUT record: Successful =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=authority",
      readFile("mockdata/response/specifications/specificationAuthority.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    if (filePath.equals(QM_RECORD_EDIT_BIB_PATH)) {
      mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);
    }

    QuickMarcEdit quickMarcJson = readQuickMarc(filePath, QuickMarcEdit.class)
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

    if (filePath.equals(QM_RECORD_EDIT_BIB_PATH)) {
      wireMockServer.verify(exactly(1), putRequestedFor(urlEqualTo(linksByInstanceIdPath(EXISTED_EXTERNAL_ID))));
    } else {
      wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(linksByInstanceIdPath(EXISTED_EXTERNAL_ID))));
    }
  }

  @Test
  void testUpdateQuickMarcRecordFailedInEvent() throws Exception {
    log.info("===== Verify PUT record: Failed in external modules =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
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

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
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

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
      .parsedRecordDtoId(wrongUuid)
      .externalId(EXISTED_EXTERNAL_ID);

    wireMockServer.verify(exactly(0), putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(wrongUuid))));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateQuickMarcRecordIdsNotEqual() throws Exception {
    log.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
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

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    quickMarcJson.getFields().getFirst().setTag("001-invalid");

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

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    var field = getFieldWithIndicators(Collections.singletonList(" "));

    var field245 = getFieldWithValue("245", "$a content");
    var field246 = getFieldWithValue("246", "$a content");
    var field001 = getFieldWithValue("001", "$a content");

    var quickMarcJson =
      getQuickMarcJsonWithMinContent(field, field, field245, field246, field001).parsedRecordDtoId(UUID.randomUUID())
        .marcFormat(MarcFormat.BIBLIOGRAPHIC)
        .relatedRecordVersion("1")
        .parsedRecordId(VALID_PARSED_RECORD_ID)
        .externalId(UUID.randomUUID());

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors.size()").value(2))
      .andExpect(jsonPath("$.errors[0].message").value("Should have exactly 2 indicators"))
      .andExpect(jsonPath("$.errors[0].type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(jsonPath("$.errors[0].parameters[0].key").value("333"))

      .andExpect(jsonPath("$.errors[1].message").value("Is required tag"))
      .andExpect(jsonPath("$.errors[1].type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(jsonPath("$.errors[1].parameters[0].key").value("008"));

    wireMockServer.verify(exactly(0),
      putRequestedFor(urlEqualTo(changeManagerResourceByIdPath(VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testUpdateQuickMarcRecordInvalidFixedFieldItemLength() throws Exception {
    log.info("===== Verify PUT record: Invalid fixed length field items =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
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

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);
    mockPut(linksByInstanceIdPath(EXISTED_EXTERNAL_ID), SC_ACCEPTED, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class)
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
  @ValueSource(strings = {QM_RECORD_EDIT_BIB_PATH, QM_RECORD_EDIT_AUTHORITY_PATH})
  void testUpdateReturn422WhenRecordWithMultiple001(String filePath) throws Exception {
    log.info("===== Verify PUT record: 001 tag check =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=authority",
      readFile("mockdata/response/specifications/specificationAuthority.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(filePath, QuickMarcEdit.class)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    // Now we add the new 001 field to the record and try to update existing record
    quickMarcJson.getFields().add(new FieldItem().tag("001").content("$a test value"));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.issues.size()").value(1))
      .andExpect(jsonPath("$.issues[0].tag").value("001[1]"))
      .andExpect(jsonPath("$.issues[0].severity").value("error"))
      .andExpect(jsonPath("$.issues[0].definitionType").value("field"))
      .andExpect(jsonPath("$.issues[0].message").value("Field is non-repeatable."));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_EDIT_BIB_PATH, QM_RECORD_EDIT_AUTHORITY_PATH})
  void testUpdateReturn422WhenRecordWithout001Field(String filePath) throws Exception {
    log.info("===== Verify PUT record: 001 tag missed check =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=authority",
      readFile("mockdata/response/specifications/specificationAuthority.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(filePath, QuickMarcEdit.class)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    // remove the 001 field from the record and try to update existing record
    quickMarcJson.getFields().removeIf(field -> field.getTag().equals("001"));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.issues.size()").value(1))
      .andExpect(jsonPath("$.issues[0].tag").value("001[0]"))
      .andExpect(jsonPath("$.issues[0].severity").value("error"))
      .andExpect(jsonPath("$.issues[0].definitionType").value("field"))
      .andExpect(jsonPath("$.issues[0].message").value("Field 001 is required."));
  }

  @Test
  void testUpdateReturn422WhenHoldingsRecordWithMultiple001() throws Exception {
    log.info("===== Verify PUT record: multiply 001 tag check =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(QM_RECORD_EDIT_HOLDINGS_PATH, QuickMarcEdit.class)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    // Now we add the new 001 field to the record and try to update existing record
    quickMarcJson.getFields().add(new FieldItem().tag("001").content("$a test value"));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo(IS_UNIQUE_TAG_ERROR_MSG)));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_EDIT_BIB_PATH, QM_RECORD_EDIT_HOLDINGS_PATH, QM_RECORD_EDIT_AUTHORITY_PATH})
  void testUpdateReturn422WhenRecordMissed008(String filePath) throws Exception {
    log.info("===== Verify PUT record: 008 tag check =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=authority",
      readFile("mockdata/response/specifications/specificationAuthority.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    var quickMarcJson = readQuickMarc(filePath, QuickMarcEdit.class);

    // Now we remove the 008 field from the record and try to update existing record
    quickMarcJson.getFields().removeIf(field -> field.getTag().equals("008"));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(errorMessageMatch(equalTo(IS_REQUIRED_TAG_ERROR_MSG)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"relatedRecordVersion", "externalHrid", "externalId", "parsedRecordDtoId", "parsedRecordId"})
  void testUpdateReturn400WhenRecordDoNotHaveRequiredFields(String fieldName) throws Exception {
    log.info("===== Verify PUT record: required field check =====");

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    var quickMarcEdit = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);

    // set field to null
    Arrays.stream(ReflectionUtils.getAllDeclaredMethods(QuickMarcEdit.class))
      .filter(method -> method.getName().equals(fieldName))
      .findFirst()
      .ifPresent(method -> ReflectionUtils.invokeMethod(method, quickMarcEdit, (Object) null));

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcEdit)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo(String.format("Parameter '%s' must not be null", fieldName))));
  }

  @ParameterizedTest
  @ValueSource(strings = {QM_RECORD_EDIT_BIB_PATH, QM_RECORD_EDIT_HOLDINGS_PATH, QM_RECORD_EDIT_AUTHORITY_PATH})
  void testUpdateReturn400WhenRecordHaveIncorrectFieldContent(String filePath) throws Exception {
    log.info("===== Verify PUT record: Subfield length check =====");

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);

    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=authority",
      readFile("mockdata/response/specifications/specificationAuthority.json"), SC_OK, wireMockServer);

    mockPut(changeManagerResourceByIdPath(VALID_PARSED_RECORD_DTO_ID), SC_ACCEPTED, wireMockServer);

    QuickMarcEdit quickMarcJson = readQuickMarc(filePath, QuickMarcEdit.class)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    // Now change the content of the field to wrong one
    quickMarcJson.getFields().get(7).setContent("$a");

    putResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo("Subfield length")));
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
