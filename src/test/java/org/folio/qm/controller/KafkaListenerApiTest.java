package org.folio.qm.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_MINUTE;

import static org.folio.qm.support.utils.DBTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.support.utils.DBTestUtils.getCreationStatusById;
import static org.folio.qm.support.utils.DBTestUtils.saveCreationStatus;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.DI_COMPLETE_AUTHORITY;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.VALID_JOB_EXECUTION_ID;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.support.extension.ClearTable;
import org.folio.qm.support.types.IntegrationTest;

@IntegrationTest
class KafkaListenerApiTest extends BaseApiTest {

  private static final String DI_ERROR_EVENT = "mockdata/request/di-event/error-event.json";

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEventWithInstance() {
    shouldUpdateExistingStatusWhenReceivedDICompletedEvent(
      "mockdata/request/di-event/complete-event-with-instance.json");
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEventWithHoldings() {
    shouldUpdateExistingStatusWhenReceivedDICompletedEvent(
      "mockdata/request/di-event/complete-event-with-holdings.json");
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEventWithAuthority() {
    shouldUpdateExistingStatusWhenReceivedDICompletedEvent(DI_COMPLETE_AUTHORITY);
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEventWithoutExternalId() {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId, VALID_JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDIKafkaRecord("mockdata/request/di-event/complete-event-without-external-record.json", DI_COMPLETE_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("externalId", "marcId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("errorMessage", "Instance ID is missed in event payload")
      .hasFieldOrPropertyWithValue("jobExecutionId", VALID_JOB_EXECUTION_ID);
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEventWithInvalidJson() {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId, VALID_JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDIKafkaRecord("mockdata/request/di-event/complete-event-with-invalid-json.json", DI_COMPLETE_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("externalId", "marcId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("jobExecutionId", VALID_JOB_EXECUTION_ID)
      .extracting("errorMessage").asString().contains("Failed to process json with message");
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDIErrorEvent() {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId, VALID_JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDIKafkaRecord(DI_ERROR_EVENT, DI_ERROR_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("externalId", "marcId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("errorMessage", "Instance was not created")
      .hasFieldOrPropertyWithValue("jobExecutionId", VALID_JOB_EXECUTION_ID);
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldDeleteRecordWhenReceivedDICompletedEvent() {
    UUID statusId = UUID.randomUUID();
    saveCreationStatus(statusId, VALID_JOB_EXECUTION_ID, metadata, jdbcTemplate);

    sendDIKafkaRecord(DI_COMPLETE_AUTHORITY, DI_COMPLETE_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.CREATED);

    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED);
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldDeleteRecordWhenReceivedDIErrorEvent() {
    UUID statusId = UUID.randomUUID();
    saveCreationStatus(statusId, VALID_JOB_EXECUTION_ID, metadata, jdbcTemplate);

    sendDIKafkaRecord(DI_ERROR_EVENT, DI_ERROR_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);

    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR);
  }

  private void shouldUpdateExistingStatusWhenReceivedDICompletedEvent(String eventMockPath) {
    var statusId = UUID.randomUUID();
    var expectedExternalId = UUID.fromString("04b557bc-7c5e-4050-b95d-7510293caa8b");
    var expectedMarcId = UUID.fromString("55a76b7b-841d-45b9-9e64-d0827b9e2480");
    saveCreationStatus(statusId, VALID_JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDIKafkaRecord(eventMockPath, DI_COMPLETE_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.CREATED);
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("errorMessage")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED)
      .hasFieldOrPropertyWithValue("jobExecutionId", VALID_JOB_EXECUTION_ID)
      .hasFieldOrPropertyWithValue("externalId", expectedExternalId)
      .hasFieldOrPropertyWithValue("marcId", expectedMarcId);
  }

  private void awaitStatusChanged(UUID statusId, RecordCreationStatusEnum status) {
    await().atMost(ONE_MINUTE)
      .untilAsserted(() -> assertThat(getCreationStatusById(statusId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(status)
      );
  }

}
