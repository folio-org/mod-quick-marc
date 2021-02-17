package org.folio.qm.service.impl;

import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.DataImportEventProcessingService;
import org.folio.qm.util.DIEventUtils;
import org.folio.rest.jaxrs.model.DataImportEventPayload;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportEventProcessingServiceImpl implements DataImportEventProcessingService {

  private final CreationStatusService statusService;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void processDICompleted(DataImportEventPayload data) {
    processDIEvent(data, recordCreationStatus ->
      DIEventUtils.extractInstanceId(data, objectMapper)
        .ifPresentOrElse(instanceId -> {
            recordCreationStatus.setStatus(RecordCreationStatusEnum.CREATED);
            recordCreationStatus.setInstanceId(instanceId);
          }, () -> recordCreationStatus.setStatus(RecordCreationStatusEnum.ERROR)
        )
    );
  }

  @Override
  @Transactional
  public void processDIError(DataImportEventPayload data) {
    processDIEvent(data, recordCreationStatus -> recordCreationStatus.setStatus(RecordCreationStatusEnum.ERROR));
  }

  private void processDIEvent(DataImportEventPayload data, Consumer<RecordCreationStatus> statusConsumer) {
    log.info("Process [{}] event for jobExecutionId [{}]", data.getEventType(), data.getJobExecutionId());
    statusService.findByJobExecutionId(UUID.fromString(data.getJobExecutionId())).ifPresent(statusConsumer);
  }
}
