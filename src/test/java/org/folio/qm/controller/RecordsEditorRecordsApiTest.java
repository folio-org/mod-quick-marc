package org.folio.qm.controller;

import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.Error;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.QuickMarcFields;
import org.folio.qm.util.ErrorCodes;
import org.folio.qm.util.ErrorUtils;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NOT_IMPLEMENTED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.folio.qm.utils.TestUtils.EXISTED_INSTANCE_ID;
import static org.folio.qm.utils.TestUtils.INSTANCE_ID;
import static org.folio.qm.utils.TestUtils.PARSED_RECORD_DTO_PATH;
import static org.folio.qm.utils.TestUtils.QM_EDITED_RECORD_PATH;
import static org.folio.qm.utils.TestUtils.QM_LEADER_MISMATCH1;
import static org.folio.qm.utils.TestUtils.QM_LEADER_MISMATCH2;
import static org.folio.qm.utils.TestUtils.QM_RECORD_PATH;
import static org.folio.qm.utils.TestUtils.QM_WRONG_ITEM_LENGTH;
import static org.folio.qm.utils.TestUtils.VALID_PARSED_RECORD_DTO_ID;
import static org.folio.qm.utils.TestUtils.VALID_PARSED_RECORD_ID;
import static org.folio.qm.utils.TestUtils.changeManagerPath;
import static org.folio.qm.utils.TestUtils.changeManagerResourceByIdPath;
import static org.folio.qm.utils.TestUtils.getFieldWithIndicators;
import static org.folio.qm.utils.TestUtils.getJsonObject;
import static org.folio.qm.utils.TestUtils.getQuickMarcJsonWithMinContent;
import static org.folio.qm.utils.TestUtils.mockGet;
import static org.folio.qm.utils.TestUtils.mockPut;
import static org.folio.qm.utils.TestUtils.readQuickMark;
import static org.folio.qm.utils.TestUtils.recordsEditorPath;
import static org.folio.qm.utils.TestUtils.recordsEditorResourceByIdPath;
import static org.folio.qm.utils.TestUtils.verifyDateTimeUpdating;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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

    QuickMarc quickMarcJson = readQuickMark(QM_RECORD_PATH)
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

    QuickMarc quickMarcJson = readQuickMark(QM_RECORD_PATH)
      .parsedRecordDtoId(wrongUUID)
      .instanceId(EXISTED_INSTANCE_ID);

    verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson, SC_NOT_FOUND);
  }

  @Test
  void testUpdateQuickMarcRecordIdsNotEqual() {
    log.info("===== Verify PUT record: Request id and externalDtoId are not equal =====");

    QuickMarc quickMarcJson = readQuickMark(QM_RECORD_PATH)
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

    QuickMarc quickMarcJson = readQuickMark(QM_WRONG_ITEM_LENGTH)
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

    QuickMarc quickMarcJson = readQuickMark(filename)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    Error error =
      verifyPut(recordsEditorResourceByIdPath(VALID_PARSED_RECORD_ID), quickMarcJson, SC_UNPROCESSABLE_ENTITY)
        .as(Error.class);
    assertThat(error.getCode(), equalTo(ErrorCodes.LEADER_AND_008_MISMATCHING.name()));
  }

  @Test
  void testPostQuickMarkValidRecordAccepted(){
    log.info("===== Verify POST record: Successful =====");
    QuickMarc quickMarcJson = readQuickMark(QM_EDITED_RECORD_PATH)
      .parsedRecordDtoId(VALID_PARSED_RECORD_DTO_ID)
      .instanceId(EXISTED_INSTANCE_ID);

    Response response = verifyPost(recordsEditorPath(), quickMarcJson, SC_NOT_IMPLEMENTED);
    assertThat(response.getStatusCode(), equalTo(SC_NOT_IMPLEMENTED));
  }
}
