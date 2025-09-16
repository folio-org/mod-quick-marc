package org.folio.it.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_MINUTE;
import static org.folio.support.utils.DataBaseTestUtils.RECORD_CREATION_STATUS_TABLE_NAME;
import static org.folio.support.utils.DataBaseTestUtils.getCreationStatusById;
import static org.folio.support.utils.DataBaseTestUtils.saveCreationStatus;
import static org.folio.support.utils.TestEntitiesUtils.DI_EVENT_WITH_HOLDINGS;
import static org.folio.support.utils.TestEntitiesUtils.DI_EVENT_WITH_INSTANCE;
import static org.folio.support.utils.TestEntitiesUtils.JOB_EXECUTION_ID;

import java.util.UUID;
import org.folio.it.BaseIT;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.utils.TestEntitiesUtils;
import org.junit.jupiter.api.Test;

@IntegrationTest
@DatabaseCleanup(tables = RECORD_CREATION_STATUS_TABLE_NAME)
class KafkaListenerIT extends BaseIT {

  @Test
  void shouldUpdateExistingStatusWhenReceivedDataImportCompletedEventWithInstance() {
    shouldUpdateExistingStatusWhenReceivedDataImportCompletedEvent(DI_EVENT_WITH_INSTANCE);
  }

  @Test
  void shouldUpdateExistingStatusWhenReceivedDataImportCompletedEventWithHoldings() {
    shouldUpdateExistingStatusWhenReceivedDataImportCompletedEvent(DI_EVENT_WITH_HOLDINGS);
  }

  @Test
  void shouldUpdateExistingStatusWhenReceivedDataImportCompletedEventWithoutExternalId() {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId.toString(), JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDataImportKafkaRecord(TestEntitiesUtils.DI_EVENT_WITHOUT_RECORD, DI_COMPLETE_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("externalId", "marcId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("errorMessage", "Instance ID is missed in event payload")
      .hasFieldOrPropertyWithValue("jobExecutionId", UUID.fromString(JOB_EXECUTION_ID));
  }

  @Test
  void shouldUpdateExistingStatusWhenReceivedDataImportCompletedEventWithInvalidJson() {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId.toString(), JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDataImportKafkaRecord(TestEntitiesUtils.DI_EVENT_WITH_INVALID_JSON, DI_COMPLETE_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("externalId", "marcId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("jobExecutionId", UUID.fromString(JOB_EXECUTION_ID))
      .extracting("errorMessage").asString().contains("Failed to process json with message");
  }

  @Test
  void shouldUpdateExistingStatusWhenReceivedDataImportErrorEvent() {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId.toString(), JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDataImportKafkaRecord(TestEntitiesUtils.DI_ERROR_EVENT, DI_ERROR_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("externalId", "marcId")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR)
      .hasFieldOrPropertyWithValue("errorMessage", "Instance was not created")
      .hasFieldOrPropertyWithValue("jobExecutionId", UUID.fromString(JOB_EXECUTION_ID));
  }

  @Test
  void shouldDeleteRecordWhenReceivedDataImportErrorEvent() {
    UUID statusId = UUID.randomUUID();
    saveCreationStatus(statusId.toString(), JOB_EXECUTION_ID, metadata, jdbcTemplate);

    sendDataImportKafkaRecord(TestEntitiesUtils.DI_ERROR_EVENT, DI_ERROR_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.ERROR);

    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.ERROR);
  }

  private void shouldUpdateExistingStatusWhenReceivedDataImportCompletedEvent(String eventMockPath) {
    var statusId = UUID.randomUUID();
    saveCreationStatus(statusId.toString(), JOB_EXECUTION_ID, metadata, jdbcTemplate);
    sendDataImportKafkaRecord(eventMockPath, DI_COMPLETE_TOPIC_NAME);
    awaitStatusChanged(statusId, RecordCreationStatusEnum.CREATED);
    var expectedMarcId = UUID.fromString("55a76b7b-841d-45b9-9e64-d0827b9e2480");
    var expectedExternalId = UUID.fromString("04b557bc-7c5e-4050-b95d-7510293caa8b");
    var creationStatus = getCreationStatusById(statusId, metadata, jdbcTemplate);
    assertThat(creationStatus)
      .hasNoNullFieldsOrPropertiesExcept("errorMessage")
      .hasFieldOrPropertyWithValue("id", statusId)
      .hasFieldOrPropertyWithValue("status", RecordCreationStatusEnum.CREATED)
      .hasFieldOrPropertyWithValue("jobExecutionId", UUID.fromString(JOB_EXECUTION_ID))
      .hasFieldOrPropertyWithValue("externalId", expectedExternalId)
      .hasFieldOrPropertyWithValue("marcId", expectedMarcId);
  }

  private void awaitStatusChanged(UUID statusId, RecordCreationStatusEnum status) {
    await().atMost(ONE_MINUTE)
      .untilAsserted(() -> assertThat(getCreationStatusById(statusId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(status));
  }
}
