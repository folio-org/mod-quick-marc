package org.folio.qm.controller;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.support.utils.APITestUtils.ACTION_ID_PARAM;
import static org.folio.qm.support.utils.APITestUtils.EXTERNAL_ID;
import static org.folio.qm.support.utils.APITestUtils.changeManagerPath;
import static org.folio.qm.support.utils.APITestUtils.changeManagerRecordsPath;
import static org.folio.qm.support.utils.APITestUtils.mockGet;
import static org.folio.qm.support.utils.APITestUtils.mockPost;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorPath;
import static org.folio.qm.support.utils.APITestUtils.recordsEditorStatusPath;
import static org.folio.qm.support.utils.APITestUtils.usersByIdPath;
import static org.folio.qm.support.utils.DBTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.support.utils.DBTestUtils.saveCreationStatus;
import static org.folio.qm.support.utils.IOTestUtils.readFile;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.EXISTED_EXTERNAL_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.USER_JOHN_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_PARSED_RECORD_ID;

import java.util.UUID;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.RecordActionStatus;
import org.folio.qm.support.extension.ClearTable;
import org.folio.qm.support.types.IntegrationTest;
import org.folio.qm.support.utils.APITestUtils;
import org.folio.qm.support.utils.testentities.TestEntitiesUtils;
import org.folio.qm.util.ErrorUtils;

@Log4j2
@IntegrationTest
class GetRecordsApiTest extends BaseApiTest {

  public static Stream<Arguments> testDataToPositiveGetRecords() {
    return Stream.of(Arguments.arguments(PARSED_RECORD_BIB_DTO_PATH, MarcFormat.BIBLIOGRAPHIC),
      Arguments.arguments(PARSED_RECORD_HOLDINGS_DTO_PATH, MarcFormat.HOLDINGS),
      Arguments.arguments(PARSED_RECORD_AUTHORITY_DTO_PATH, MarcFormat.AUTHORITY));
  }

  @MethodSource("testDataToPositiveGetRecords")
  @ParameterizedTest(name = "[{index}] verify GET {1} record")
  void testGetQuickMarcRecord(String parsedRecordAuthorityDtoPath, MarcFormat marcFormat) throws Exception {
    mockGet(changeManagerPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID), readFile(parsedRecordAuthorityDtoPath), SC_OK,
      mockServer);
    mockGet(usersByIdPath(JOHN_USER_ID), readFile(USER_JOHN_PATH), SC_OK, mockServer);
    mockGet(APITestUtils.FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      mockServer);
    mockPost(changeManagerRecordsPath(VALID_JOB_EXECUTION_ID), SC_OK, mockServer);

    performGet(recordsEditorPath(EXTERNAL_ID, EXISTED_EXTERNAL_ID)).andExpect(status().isOk())
      .andExpect(jsonPath("$.marcFormat").value(marcFormat.getValue()))
      .andExpect(jsonPath("$.parsedRecordDtoId").value(VALID_PARSED_RECORD_DTO_ID.toString()))
      .andExpect(jsonPath("$.externalId").value(EXISTED_EXTERNAL_ID.toString()))
      .andExpect(jsonPath("$.suppressDiscovery").value(Boolean.FALSE))
      .andExpect(jsonPath("$.parsedRecordId").value(VALID_PARSED_RECORD_ID.toString()))
      .andExpect(jsonPath("$.updateInfo.updatedBy.userId").value(JOHN_USER_ID));
  }

  @Test
  void testGetQuickMarcRecordNotFound() throws Exception {
    log.info("===== Verify GET record: Record Not Found =====");

    UUID recordNotFoundId = UUID.randomUUID();

    mockGet(changeManagerPath(EXTERNAL_ID, recordNotFoundId), "Not found", SC_NOT_FOUND, mockServer);

    performGet(recordsEditorPath(EXTERNAL_ID, recordNotFoundId))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED.getTypeCode()))
      .andExpect(errorHasMessage("Not found"));

    assertThat(mockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordConverterError() throws Exception {
    log.info("===== Verify GET record: Converter (quickMARC internal exception) =====");

    UUID instanceId = UUID.randomUUID();

    mockGet(changeManagerPath(EXTERNAL_ID, instanceId), "{\"recordType\": \"MARC_BIB\"}", SC_OK, mockServer);
    mockGet(APITestUtils.FIELD_PROTECTION_SETTINGS_PATH, readFile(TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_PATH), SC_OK,
      mockServer);

    performGet(recordsEditorPath(EXTERNAL_ID, instanceId))
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(errorHasMessage("Generic Error"));

    assertThat(mockServer.getAllServeEvents(), hasSize(1));
  }

  @Test
  void testGetQuickMarcRecordWithoutInstanceIdParameter() throws Exception {
    log.info("===== Verify GET record: Request without instanceId =====");

    UUID id = UUID.randomUUID();

    performGet(recordsEditorPath("X", id))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()))
      .andExpect(errorHasMessage("Parameter 'externalId' is required"));

    assertThat(mockServer.getAllServeEvents(), hasSize(0));
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void testGetCreationStatus() throws Exception {
    log.info("===== Verify GET record status: Successful =====");

    var id = UUID.randomUUID();
    saveCreationStatus(id, id, metadata, jdbcTemplate);

    performGet(recordsEditorStatusPath(ACTION_ID_PARAM, id.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.actionId").value(id.toString()))
      .andExpect(jsonPath("$.status").value(RecordActionStatus.StatusEnum.NEW.getValue()))
      .andExpect(jsonPath("$.metadata").value(notNullValue()))
      .andExpect(jsonPath("$.metadata.createdAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+")));
  }

  @Test
  void testReturn404IfStatusNotFound() throws Exception {
    log.info("===== Verify GET record status: Not found =====");

    var notExistedId = UUID.randomUUID().toString();

    performGet(recordsEditorStatusPath(ACTION_ID_PARAM, notExistedId))
      .andExpect(status().isNotFound())
      .andExpect(errorHasMessage("not found"));
  }

  @Test
  void testReturn400IfActionIdIsInvalid() throws Exception {
    log.info("===== Verify GET record status: Parameter invalid =====");

    var invalidId = "invalid";

    performGet(recordsEditorStatusPath(ACTION_ID_PARAM, invalidId))
      .andExpect(status().isBadRequest())
      .andExpect(errorHasMessage("Parameter 'actionId' is invalid"));
  }

  @Test
  void testReturn400IfActionIdIsMissing() throws Exception {
    log.info("===== Verify GET record status: Parameter missing =====");

    performGet(recordsEditorStatusPath())
      .andExpect(status().isBadRequest())
      .andExpect(errorHasMessage("Parameter 'actionId' is required"));
  }

  @Test
  void testReturn400IfActionIdIsEmpty() throws Exception {
    log.info("===== Verify GET record status: Parameter empty =====");

    var id = "";
    performGet(recordsEditorStatusPath(ACTION_ID_PARAM, id))
      .andExpect(status().isBadRequest())
      .andExpect(errorHasMessage("Parameter 'actionId' is required"));
  }

}
