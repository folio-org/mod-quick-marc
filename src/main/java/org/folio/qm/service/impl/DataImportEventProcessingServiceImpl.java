package org.folio.qm.service.impl;

import static org.folio.qm.util.DIEventUtils.extractErrorMessage;
import static org.folio.qm.util.DIEventUtils.extractInstanceId;
import static org.folio.qm.util.DIEventUtils.extractMarcBibId;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.DataImportEventProcessingService;
import org.folio.rest.jaxrs.model.DataImportEventPayload;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportEventProcessingServiceImpl implements DataImportEventProcessingService {

  private static final String INSTANCE_ID_MISSED_MESSAGE = "Instance ID is missed in event payload";
  private static final String ERROR_MISSED_MESSAGE = "Error message is missed in event payload";

  private final CreationStatusService statusService;
  private final ObjectMapper objectMapper;

  @Override
  public void processDICompleted(DataImportEventPayload data) {
    var updateBuilder = RecordCreationStatusUpdate.builder();
    try {
      extractMarcBibId(data, objectMapper).ifPresent(updateBuilder::marcBibId);
      extractInstanceId(data, objectMapper)
        .ifPresentOrElse(instanceId -> updateBuilder.status(RecordCreationStatusEnum.CREATED).instanceId(instanceId),
          () -> {
            var errorMessage = extractErrorMessage(data).orElse(INSTANCE_ID_MISSED_MESSAGE);
            updateBuilder.status(RecordCreationStatusEnum.ERROR).errorMessage(errorMessage);
          }
        );
    } catch (IllegalStateException e) {
      updateBuilder.status(RecordCreationStatusEnum.ERROR).errorMessage(e.getMessage());
    }
    processDIEvent(data, updateBuilder.build());
  }

  @Override
  public void processDIError(DataImportEventPayload data) {
    var errorMessage = extractErrorMessage(data).orElse(ERROR_MISSED_MESSAGE);
    var updateBuilder = RecordCreationStatusUpdate.builder()
      .status(RecordCreationStatusEnum.ERROR)
      .errorMessage(errorMessage);
    processDIEvent(data, updateBuilder.build());
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
