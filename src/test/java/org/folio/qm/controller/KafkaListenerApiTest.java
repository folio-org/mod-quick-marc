package org.folio.qm.controller;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import static org.folio.qm.utils.TestDBUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.qm.utils.TestDBUtils.getCreationStatusById;
import static org.folio.qm.utils.TestDBUtils.saveCreationStatus;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.extension.ClearTable;

public class KafkaListenerApiTest extends BaseApiTest {

  private static final UUID JOB_EXECUTION_ID = UUID.fromString("a7fb1c32-1ffb-4a22-a76a-4067284fe68d");

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEvent() throws IOException {
    var statusId = UUID.randomUUID();
    var expectedInstanceId = UUID.fromString("04b557bc-7c5e-4050-b95d-7510293caa8b");
    saveCreationStatus(statusId, JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendKafkaRecord("mockdata/di-event/complete-event.json", COMPLETE_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> assertThat(getCreationStatusById(statusId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.CREATED)
      );
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("errorMessage")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED)
      .hasFieldOrPropertyWithValue("jobExecutionId", JOB_EXECUTION_ID)
      .hasFieldOrPropertyWithValue("instanceId", expectedInstanceId);
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEventWithoutInstanceId() throws IOException {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId, JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendKafkaRecord("mockdata/di-event/complete-event-without-instance.json", COMPLETE_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> assertThat(getCreationStatusById(statusId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.ERROR)
      );
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("instanceId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("errorMessage", "Instance ID is missed in event payload")
      .hasFieldOrPropertyWithValue("jobExecutionId", JOB_EXECUTION_ID);
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDICompletedEventWithInvalidJson() throws IOException {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId, JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendKafkaRecord("mockdata/di-event/complete-event-with-invalid-json.json", COMPLETE_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> assertThat(getCreationStatusById(statusId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.ERROR)
      );
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("instanceId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("jobExecutionId", JOB_EXECUTION_ID)
      .extracting("errorMessage").asString().contains("Failed to process json with message");
  }

  @Test
  @ClearTable(RECORD_CREATION_STATUS_TABLE_NAME)
  void shouldUpdateExistingStatusWhenReceivedDIErrorEvent() throws IOException {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId, JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendKafkaRecord("mockdata/di-event/error-event.json", ERROR_TOPIC_NAME);
    await().atMost(5, SECONDS)
      .untilAsserted(() -> assertThat(getCreationStatusById(statusId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(RecordCreationStatusEnum.ERROR)
      );
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("instanceId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("errorMessage", "Instance was not created")
      .hasFieldOrPropertyWithValue("jobExecutionId", JOB_EXECUTION_ID);
  }
}
