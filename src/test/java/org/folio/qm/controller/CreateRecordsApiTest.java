package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.support.utils.APITestUtils.ACTION_ID_PARAM;
import static org.folio.qm.support.utils.APITestUtils.CHANGE_MANAGER_JOB_EXECUTION_PATH;
import static org.folio.qm.support.utils.APITestUtils.JOHN_USER_ID_HEADER;
import static org.folio.qm.support.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.support.utils.APITestUtils.changeManagerJobExecutionPath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerJobProfilePath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerRecordsPath;
import static org.folio.qm.support.utils.APITestUtils.mockPost;
import static org.folio.qm.support.utils.APITestUtils.mockPut;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorPath;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorStatusPath;
import static org.folio.qm.support.utils.DBTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectFromJson;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_HOLDINGS;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_INSTANCE;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOB_EXECUTION_CREATED;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOB_EXECUTION_WITH_INVALID_USER;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;

import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.http.Fault;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.RecordActionStatus;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.support.extension.ClearTable;
import org.folio.qm.support.types.IntegrationTest;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.integration.XOkapiHeaders;

@Log4j2
@IntegrationTest
class CreateRecordsApiTest extends BaseApiTest {

  public static Stream<Arguments> testCreateRecordTestData() {
    return Stream.of(
      Arguments.arguments(QM_RECORD_BIB_PATH, DI_COMPLETE_INSTANCE, MarcFormat.BIBLIOGRAPHIC),
      Arguments.arguments(QM_RECORD_HOLDINGS_PATH, DI_COMPLETE_HOLDINGS, MarcFormat.HOLDINGS)
    );
  }

  @MethodSource("testCreateRecordTestData")
  @ParameterizedTest
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testRecordCreation(String qmRecordMockPath, String eventMockPath, MarcFormat marcFormat)    throws Exception {
    QuickMarc qmRecord = readQuickMarc(qmRecordMockPath)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    mockPost(changeManagerJobExecutionPath(), JOB_EXECUTION_CREATED, mockServer);
    mockPut(changeManagerJobProfilePath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);
    mockPost(changeManagerRecordsPath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);

    MvcResult result = performPost(recordsEditorPath(), qmRecord, JOHN_USER_ID_HEADER)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.IN_PROGRESS.getValue()))
      .andReturn();

    String resultResponse = result.getResponse().getContentAsString();
    RecordActionStatus response = getObjectFromJson(resultResponse, RecordActionStatus.class);

    final var actionId = response.getActionId();

    sendEventAndWaitStatusChange(actionId, ActionStatusEnum.COMPLETED, DI_COMPLETE_TOPIC_NAME,
      eventMockPath);

    performGet(recordsEditorStatusPath(ACTION_ID_PARAM, actionId.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.actionId").value(actionId.toString()))
      .andExpect(jsonPath("$.marcFormat").value(marcFormat.getValue()))
      .andExpect(jsonPath("$.actionType").value(RecordActionStatus.ActionTypeEnum.CREATE.getValue()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.COMPLETED.getValue()))
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.metadata").value(notNullValue()));
  }

  @Test
  void testReturn401WhenInvalidUserId() throws Exception {
    log.info("===== Verify POST record: User Id Invalid =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, JOB_EXECUTION_WITH_INVALID_USER, SC_UNPROCESSABLE_ENTITY, mockServer);

    performPost(recordsEditorPath(), qmRecord, JOHN_USER_ID_HEADER)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
      .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"));
  }

  @Test
  void testReturn400WhenNoHeader() throws Exception {
    log.info("===== Verify POST record: Bad request =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    mockMvc.perform(MockMvcRequestBuilders.post(recordsEditorPath())
        .header(XOkapiHeaders.TENANT, TENANT_ID)
        .content(getObjectAsJson(qmRecord)))
      .andDo(log())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("x-okapi-user-id header must be provided")));
  }

  @Test
  void testReturn422WhenCreateHoldingsWithMultiply852() throws Exception {
    log.info("===== Verify POST record: Multiply 852 =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_HOLDINGS_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    qmRecord.getFields().add(new FieldItem().tag("852").content("$b content"));

    performPost(recordsEditorPath(), qmRecord, JOHN_USER_ID_HEADER)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(jsonPath("$.message").value("Is unique tag"));
  }

  @Test
  void testReturn400WhenConnectionReset() throws Exception {
    log.info("===== Verify POST record: Connection reset =====");

    QuickMarc qmRecord = readQuickMarc(QM_RECORD_BIB_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .externalId(EXISTED_EXTERNAL_ID);

    mockServer.stubFor(post(urlEqualTo(CHANGE_MANAGER_JOB_EXECUTION_PATH))
      .willReturn(aResponse()
        .withStatus(SC_OK)
        .withFault(Fault.CONNECTION_RESET_BY_PEER)
      ));

    performPost(recordsEditorPath(), qmRecord, JOHN_USER_ID_HEADER)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
      .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
      .andExpect(jsonPath("$.message").value(containsString("Connection reset executing")));
  }
}
