package org.folio.qm.service.impl;

import static org.folio.qm.util.DIEventUtils.extractErrorMessage;
import static org.folio.qm.util.DIEventUtils.extractExternalId;
import static org.folio.qm.util.DIEventUtils.extractMarcId;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.EventProcessingService;
import org.folio.rest.jaxrs.model.DataImportEventPayload;

@Log4j2
@Component(value = "importEventProcessingService")
@RequiredArgsConstructor
public class DataImportEventProcessingServiceImpl implements EventProcessingService {

  private static final String INSTANCE_ID_MISSED_MESSAGE = "Instance ID is missed in event payload";
  private static final String ERROR_MISSED_MESSAGE = "Error message is missed in event payload";

  private final CreationStatusService statusService;
  private final ObjectMapper objectMapper;

  @Override
  public boolean processDICompleted(DataImportEventPayload data) {
    var result = false;
    var updateBuilder = RecordCreationStatusUpdate.builder();
    try {
      extractMarcId(data, objectMapper).ifPresent(updateBuilder::marcId);
      extractExternalId(data, objectMapper)
        .ifPresentOrElse(instanceId -> updateBuilder.status(RecordCreationStatusEnum.CREATED).externalId(instanceId),
          () -> {
            var errorMessage = extractErrorMessage(data).orElse(INSTANCE_ID_MISSED_MESSAGE);
            updateBuilder.status(RecordCreationStatusEnum.ERROR).errorMessage(errorMessage);
          }
        );
      result = true;
    } catch (IllegalStateException e) {
      updateBuilder.status(RecordCreationStatusEnum.ERROR).errorMessage(e.getMessage());
    }
    processDIEvent(data, updateBuilder.build());
    return result;
  }

  @Override
  public boolean processDIError(DataImportEventPayload data) {
    var errorMessage = extractErrorMessage(data).orElse(ERROR_MISSED_MESSAGE);
    var updateBuilder = RecordCreationStatusUpdate.builder()
      .status(RecordCreationStatusEnum.ERROR)
      .errorMessage(errorMessage);
    processDIEvent(data, updateBuilder.build());

    return true;
  }

  private void processDIEvent(DataImportEventPayload data, RecordCreationStatusUpdate statusUpdate) {
    var jobExecutionId = data.getJobExecutionId();
    log.info("Process [{}] event for jobExecutionId [{}]", data.getEventType(), jobExecutionId);
    var isUpdated = statusService.updateByJobExecutionId(UUID.fromString(jobExecutionId), statusUpdate);
    if (isUpdated) {
      log.info("Record creation status for jobExecutionId [{}] was updated with values [{}]", jobExecutionId, statusUpdate);
    }
  }
}
