package org.folio.qm.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.UUID;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;

@ExtendWith(RandomBeansExtension.class)
class CreationStatusMapperTest {

  private static final CreationStatusMapper MAPPER = Mappers.getMapper(CreationStatusMapper.class);

  @Test
  void shouldConvertSuccessfullyWhenAllFieldsArePresent(@Random UUID id, @Random UUID instanceId,
                                                        @Random UUID jobExecutionId, @Random Timestamp createdAt,
                                                        @Random Timestamp updatedAt) {
    var recordCreationStatus = new RecordCreationStatus();
    recordCreationStatus.setId(id);
    recordCreationStatus.setStatus(RecordCreationStatusEnum.NEW);
    recordCreationStatus.setInstanceId(instanceId);
    recordCreationStatus.setJobExecutionId(jobExecutionId);
    recordCreationStatus.setCreatedAt(createdAt);
    recordCreationStatus.setUpdatedAt(updatedAt);
    var creationStatus = MAPPER.fromEntity(recordCreationStatus);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("instanceId", instanceId.toString())
      .hasFieldOrPropertyWithValue("qmRecordId", id.toString())
      .hasFieldOrPropertyWithValue("jobExecutionId", jobExecutionId.toString())
      .hasFieldOrPropertyWithValue("status", CreationStatus.StatusEnum.NEW)
      .hasFieldOrPropertyWithValue("metadata.createdAt", createdAt)
      .hasFieldOrPropertyWithValue("metadata.updatedAt", updatedAt);
  }

  @Test
  void shouldConvertSuccessfullyWhenTimestampFieldsAreNull(@Random UUID id, @Random UUID instanceId,
                                                           @Random UUID jobExecutionId) {
    var recordCreationStatus = new RecordCreationStatus();
    recordCreationStatus.setId(id);
    recordCreationStatus.setStatus(RecordCreationStatusEnum.NEW);
    recordCreationStatus.setInstanceId(instanceId);
    recordCreationStatus.setJobExecutionId(jobExecutionId);
    var creationStatus = MAPPER.fromEntity(recordCreationStatus);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("instanceId", instanceId.toString())
      .hasFieldOrPropertyWithValue("qmRecordId", id.toString())
      .hasFieldOrPropertyWithValue("jobExecutionId", jobExecutionId.toString())
      .hasFieldOrPropertyWithValue("status", CreationStatus.StatusEnum.NEW)
      .hasFieldOrPropertyWithValue("metadata.createdAt", null)
      .hasFieldOrPropertyWithValue("metadata.updatedAt", null);
  }

  @Test
  void shouldConvertSuccessfullyWhenUUIDFieldsAreNull(@Random Timestamp createdAt, @Random Timestamp updatedAt) {
    var recordCreationStatus = new RecordCreationStatus();
    recordCreationStatus.setStatus(RecordCreationStatusEnum.NEW);
    recordCreationStatus.setCreatedAt(createdAt);
    recordCreationStatus.setUpdatedAt(updatedAt);
    var creationStatus = MAPPER.fromEntity(recordCreationStatus);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("instanceId", null)
      .hasFieldOrPropertyWithValue("qmRecordId", null)
      .hasFieldOrPropertyWithValue("jobExecutionId", null)
      .hasFieldOrPropertyWithValue("status", CreationStatus.StatusEnum.NEW)
      .hasFieldOrPropertyWithValue("metadata.createdAt", createdAt)
      .hasFieldOrPropertyWithValue("metadata.updatedAt", updatedAt);
  }

}