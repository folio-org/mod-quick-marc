package org.folio.qm.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;

import org.folio.qm.domain.dto.ActionStatusDto;
import org.folio.qm.domain.entity.ActionStatus;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.support.types.UnitTest;

@UnitTest
@ExtendWith(RandomBeansExtension.class)
class ActionStatusMapperTest {

  private static final ActionStatusMapper MAPPER = Mappers.getMapper(ActionStatusMapper.class);

  @Test
  void shouldConvertSuccessfullyWhenAllFieldsArePresent(@Random UUID id, @Random UUID externalId,
                                                        @Random UUID jobExecutionId, @Random Timestamp createdAt,
                                                        @Random Timestamp updatedAt) {
    var recordCreationStatus = new ActionStatus();
    recordCreationStatus.setId(id);
    recordCreationStatus.setStatus(ActionStatusEnum.IN_PROGRESS);
    recordCreationStatus.setExternalId(externalId);
    recordCreationStatus.setJobExecutionId(jobExecutionId);
    recordCreationStatus.setCreatedAt(createdAt);
    recordCreationStatus.setUpdatedAt(updatedAt);
    var creationStatus = MAPPER.fromEntity(recordCreationStatus);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("externalId", externalId)
      .hasFieldOrPropertyWithValue("actionId", id)
      .hasFieldOrPropertyWithValue("jobExecutionId", jobExecutionId)
      .hasFieldOrPropertyWithValue("status", ActionStatusDto.StatusEnum.IN_PROGRESS)
      .hasFieldOrPropertyWithValue("metadata.createdAt", getFrom(createdAt))
      .hasFieldOrPropertyWithValue("metadata.updatedAt", getFrom(updatedAt));
  }

  @Test
  void shouldConvertSuccessfullyWhenTimestampFieldsAreNull(@Random UUID id, @Random UUID externalId,
                                                           @Random UUID jobExecutionId) {
    var recordCreationStatus = new ActionStatus();
    recordCreationStatus.setId(id);
    recordCreationStatus.setStatus(ActionStatusEnum.IN_PROGRESS);
    recordCreationStatus.setExternalId(externalId);
    recordCreationStatus.setJobExecutionId(jobExecutionId);
    var creationStatus = MAPPER.fromEntity(recordCreationStatus);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("externalId", externalId)
      .hasFieldOrPropertyWithValue("actionId", id)
      .hasFieldOrPropertyWithValue("jobExecutionId", jobExecutionId)
      .hasFieldOrPropertyWithValue("status", ActionStatusDto.StatusEnum.IN_PROGRESS)
      .hasFieldOrPropertyWithValue("metadata.createdAt", null)
      .hasFieldOrPropertyWithValue("metadata.updatedAt", null);
  }

  @Test
  void shouldConvertSuccessfullyWhenUUIDFieldsAreNull(@Random Timestamp createdAt, @Random Timestamp updatedAt) {
    var recordCreationStatus = new ActionStatus();
    recordCreationStatus.setStatus(ActionStatusEnum.IN_PROGRESS);
    recordCreationStatus.setCreatedAt(createdAt);
    recordCreationStatus.setUpdatedAt(updatedAt);
    var creationStatus = MAPPER.fromEntity(recordCreationStatus);
    assertThat(creationStatus)
      .hasFieldOrPropertyWithValue("externalId", null)
      .hasFieldOrPropertyWithValue("actionId", null)
      .hasFieldOrPropertyWithValue("jobExecutionId", null)
      .hasFieldOrPropertyWithValue("status", ActionStatusDto.StatusEnum.IN_PROGRESS)
      .hasFieldOrPropertyWithValue("metadata.createdAt", getFrom(createdAt))
      .hasFieldOrPropertyWithValue("metadata.updatedAt", getFrom(updatedAt));
  }

  private OffsetDateTime getFrom(Timestamp timestamp) {
    return OffsetDateTime.from(timestamp.toInstant().atZone(ZoneOffset.UTC));
  }
}
