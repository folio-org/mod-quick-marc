package org.folio.it.api;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.UUID.fromString;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.folio.qm.validation.FieldValidationRule.IS_REQUIRED_TAG_ERROR_MSG;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.folio.support.utils.ApiTestUtils.changeManagerResourceByIdPath;
import static org.folio.support.utils.ApiTestUtils.linksByInstanceIdPath;
import static org.folio.support.utils.ApiTestUtils.mockGet;
import static org.folio.support.utils.ApiTestUtils.recordsEditorByIdPath;
import static org.folio.support.utils.ApiTestUtils.sourceStoragePath;
import static org.folio.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.support.utils.TestEntitiesUtils.AUTHORITY_ID;
import static org.folio.support.utils.TestEntitiesUtils.HOLDINGS_ID;
import static org.folio.support.utils.TestEntitiesUtils.INSTANCE_ID;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_HOLDINGS_PATH;
import static org.folio.support.utils.TestEntitiesUtils.getFieldWithValue;
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
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.folio.it.BaseIT;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.testing.type.IntegrationTest;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.ReflectionUtils;

@Log4j2
@IntegrationTest
class RecordsEditorAsyncIT extends BaseIT {

  @ParameterizedTest
  @MethodSource("testUpdateQuickMarcRecordCases")
  @DisplayName("Should update QuickMarc record successfully")
  void testUpdateQuickMarcRecord(String filePath, String externalId) throws Exception {
    var quickMarcRecord = readQuickMarc(filePath, QuickMarcEdit.class);

    var result = doPut(recordsEditorByIdPath(externalId), quickMarcRecord)
      .andExpect(request().asyncStarted())
      .andReturn();

    sendQuickMarcKafkaRecord(createEventPayload(externalId, null));

    mockMvc.perform(asyncDispatch(result))
      .andDo(log())
      .andExpect(status().isAccepted());

    if (quickMarcRecord.getMarcFormat() == MarcFormat.BIBLIOGRAPHIC) {
      expectLinksUpdateRequests(1, linksByInstanceIdPath(externalId));
    } else {
      expectLinksUpdateRequests(0, linksByInstanceIdPath(externalId));
    }
  }

  @Test
  @DisplayName("Should fail when failed in event")
  void testUpdateQuickMarcRecordFailedInEvent() throws Exception {
    var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);

    var result = doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
      .andExpect(request().asyncStarted())
      .andReturn();

    var errorMessage = "Some error occurred";
    sendQuickMarcKafkaRecord(createEventPayload(INSTANCE_ID, errorMessage));

    mockMvc.perform(asyncDispatch(result))
      .andExpect(status().isBadRequest())
      .andDo(log())
      .andExpect(errorMessageMatch(equalTo(errorMessage)));

    expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
  }

  @Test
  @DisplayName("Should fail when failed in event by optimistic locking")
  void testUpdateQuickMarcRecordFailedInEventByOptimisticLocking() throws Exception {
    var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);

    var result = doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
      .andExpect(request().asyncStarted())
      .andReturn();

    sendQuickMarcKafkaRecord(createEventPayload(INSTANCE_ID, olMessage(INSTANCE_ID, 1, 2)));
    mockMvc
      .perform(asyncDispatch(result))
      .andExpect(status().isConflict())
      .andDo(log())
      .andExpect(optimisticLockingMessage(INSTANCE_ID, 1, 2));

    expectLinksUpdateRequests(0, linksByInstanceIdPath(INSTANCE_ID));
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
      .parsedRecordDtoId(UUID.fromString(wrongUuid))
      .externalId(UUID.fromString(wrongUuid));

    doPut(recordsEditorByIdPath(wrongUuid), quickMarcRecord)
      .andExpect(status().isNotFound());

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(wrongUuid));
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(INSTANCE_ID));
    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(pathId));
  }

  @Test
  @DisplayName("Should fail with 400 when MARC tag has alphabetic symbols")
  void testUpdateQuickMarcRecordTagIsInvalid() throws Exception {
    var quickMarcRecord = readQuickMarc(QM_RECORD_EDIT_BIB_PATH, QuickMarcEdit.class);
    quickMarcRecord.getFields().getFirst().setTag("001-invalid");

    doPut(recordsEditorByIdPath(INSTANCE_ID), quickMarcRecord)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(equalTo("Parameter 'fields[0].tag' must match \"^[0-9]{3}$\"")));

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(INSTANCE_ID));
  }

  @Test
  @DisplayName("Should fail with 400 when empty request body")
  void testUpdateQuickMarcRecordWithEmptyBody() throws Exception {
    doPut(recordsEditorByIdPath(INSTANCE_ID))
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(containsString("Required request body is missing")));

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(INSTANCE_ID));
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(INSTANCE_ID));
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(INSTANCE_ID));
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
      .andExpect(status().isOk());
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(id));
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(id));
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(HOLDINGS_ID));
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(id));
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

    expectLinksUpdateRequests(0, changeManagerResourceByIdPath(INSTANCE_ID));
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

  private String createEventPayload(String id, String errorMessage) throws JsonProcessingException {
    var payload = new QmCompletedEventPayload();
    payload.setRecordId(fromString(id));
    payload.setErrorMessage(errorMessage);
    return new ObjectMapper().writeValueAsString(payload);
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
