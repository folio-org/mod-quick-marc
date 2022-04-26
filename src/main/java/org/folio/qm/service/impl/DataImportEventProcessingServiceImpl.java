package org.folio.qm.service.impl;

import static org.folio.qm.util.DIEventUtils.extractErrorMessage;
import static org.folio.qm.util.DIEventUtils.extractExternalId;
import static org.folio.qm.util.DIEventUtils.extractMarcId;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.DataImportEventPayload;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.domain.entity.ActionStatusUpdate;
import org.folio.qm.service.StatusService;
import org.folio.qm.service.EventProcessingService;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportEventProcessingServiceImpl implements EventProcessingService {

  private static final String EXTERNAL_ID_MISSED_MESSAGE = "External ID is missed in event payload";
  private static final String ERROR_MISSED_MESSAGE = "Error message is missed in event payload";

  private final StatusService statusService;
  private final ObjectMapper objectMapper;

  @Override
  public void processDICompleted(DataImportEventPayload data) {
    var updateBuilder = ActionStatusUpdate.builder();
    try {
      extractMarcId(data, objectMapper).ifPresent(updateBuilder::marcId);
      extractExternalId(data, objectMapper)
        .ifPresentOrElse(instanceId -> updateBuilder.status(ActionStatusEnum.COMPLETED).externalId(instanceId),
          () -> {
            var errorMessage = extractErrorMessage(data).orElse(EXTERNAL_ID_MISSED_MESSAGE);
            updateBuilder.status(ActionStatusEnum.ERROR).errorMessage(errorMessage);
          }
        );
    } catch (IllegalStateException e) {
      updateBuilder.status(ActionStatusEnum.ERROR).errorMessage(e.getMessage());
    }
    processDIEvent(data, updateBuilder.build());
  }

  @Override
  public void processDIError(DataImportEventPayload data) {
    var errorMessage = extractErrorMessage(data).orElse(ERROR_MISSED_MESSAGE);
    var updateBuilder = ActionStatusUpdate.builder()
      .status(ActionStatusEnum.ERROR)
      .errorMessage(errorMessage);
    processDIEvent(data, updateBuilder.build());
  }

  private void processDIEvent(DataImportEventPayload data, ActionStatusUpdate statusUpdate) {
    var jobExecutionId = data.getJobExecutionId();
    log.info("Process [{}] event for jobExecutionId [{}]", data.getEventType(), jobExecutionId);
    var isUpdated = statusService.updateByJobExecutionId(jobExecutionId, statusUpdate);
    if (isUpdated) {
      log.info("Record creation status for jobExecutionId [{}] was updated with values [{}]", jobExecutionId,
        statusUpdate);
    }
  }

}
