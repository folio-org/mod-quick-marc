package org.folio.qm.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.support.utils.APITestUtils.ACTION_ID_PARAM;
import static org.folio.qm.support.utils.APITestUtils.CHANGE_MANAGER_JOB_EXECUTION_PATH;
import static org.folio.qm.support.utils.APITestUtils.EXTERNAL_ID;
import static org.folio.qm.support.utils.APITestUtils.changeManagerJobProfilePath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerPath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerRecordsPath;
import static org.folio.qm.support.utils.APITestUtils.mockGet;
import static org.folio.qm.support.utils.APITestUtils.mockPost;
import static org.folio.qm.support.utils.APITestUtils.mockPut;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorStatusPath;
import static org.folio.qm.support.utils.DBTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.support.utils.IOTestUtils.readFile;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectFromJson;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_AUTHORITY;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOB_EXECUTION_CREATED;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.RecordActionStatus;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.support.extension.ClearTable;
import org.folio.qm.support.types.IntegrationTest;

@Log4j2
@IntegrationTest
class DeleteRecordsApiTest extends BaseApiTest {

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testDeleteQuickMarcAuthorityRecord() throws Exception {
    log.info("===== Verify DELETE authority record: No Content");

    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(PARSED_RECORD_AUTHORITY_DTO_PATH), SC_OK,
      mockServer);
    mockPost(CHANGE_MANAGER_JOB_EXECUTION_PATH, JOB_EXECUTION_CREATED, mockServer);
    mockPut(changeManagerJobProfilePath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);
    mockPost(changeManagerRecordsPath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);

    var result = deleteResultActions(recordsEditorResourceByIdPath(EXISTED_EXTERNAL_ID))
      .andExpect(status().isOk())
      .andReturn();

    String resultResponse = result.getResponse().getContentAsString();
    RecordActionStatus response = getObjectFromJson(resultResponse, RecordActionStatus.class);
    var actionId = response.getActionId();

    sendEventAndWaitStatusChange(actionId, ActionStatusEnum.COMPLETED, DI_COMPLETE_TOPIC_NAME,
      DI_COMPLETE_AUTHORITY);

    getResultActions(recordsEditorStatusPath(ACTION_ID_PARAM, actionId.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.actionId").value(actionId.toString()))
      .andExpect(jsonPath("$.marcFormat").value(MarcFormat.AUTHORITY.getValue()))
      .andExpect(jsonPath("$.actionType").value(RecordActionStatus.ActionTypeEnum.DELETE.getValue()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.COMPLETED.getValue()))
      .andExpect(jsonPath("$.jobExecutionId").value(VALID_JOB_EXECUTION_ID.toString()))
      .andExpect(jsonPath("$.metadata").value(notNullValue()));

  }

  @ParameterizedTest
  @ValueSource(strings = {PARSED_RECORD_BIB_DTO_PATH, PARSED_RECORD_HOLDINGS_DTO_PATH})
  void testDeleteQuickMarcNoAuthorityRecord(String filename) throws Exception {
    log.info("===== Verify DELETE no authority record: Bad Request");
    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(filename), SC_OK, mockServer);

    deleteResultActions(recordsEditorResourceByIdPath(EXISTED_EXTERNAL_ID))
      .andExpect(status().isBadRequest())
      .andExpect(errorHasMessage("Job profile for [DELETE] action"));

    mockServer.verify(exactly(0),
      getRequestedFor(urlEqualTo(changeManagerPath(EXTERNAL_ID, VALID_PARSED_RECORD_ID))));
  }

  @Test
  void testDeleteQuickMarcRecordWrongUuid() throws Exception {
    log.info("===== Verify DELETE record: Not found =====");
    mockGet(changeManagerPath(EXTERNAL_ID, VALID_PARSED_RECORD_ID), "{}", SC_NOT_FOUND, mockServer);

    deleteResultActions(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID))
      .andExpect(status().isNotFound());
  }
}
