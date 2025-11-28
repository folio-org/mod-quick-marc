package org.folio.qm.service.impl;

import static org.folio.qm.util.DataImportEventUtils.extractErrorMessage;
import static org.folio.qm.util.DataImportEventUtils.extractExternalId;
import static org.folio.qm.util.DataImportEventUtils.extractMarcId;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.model.DataImportEventPayload;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;
import org.folio.qm.service.EventProcessingService;
import org.folio.qm.service.StatusService;
import org.folio.qm.util.ErrorUtils;
import org.folio.tenant.domain.dto.Error;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportEventProcessingServiceImpl implements EventProcessingService {

  private static final String INSTANCE_ID_MISSED_MESSAGE = "Instance ID is missed in event payload";
  private static final String ERROR_MISSED_MESSAGE = "Error message is missed in event payload";

  private final StatusService statusService;
  private final ObjectMapper objectMapper;
  private final DeferredResultCacheService cacheService;

  @Override
  public void processDataImportCompleted(DataImportEventPayload data) {
    log.debug("processDataImportCompleted:: trying to process [{}] event for jobExecutionId [{}]",
      data.getEventType(), data.getJobExecutionId());
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
    } catch (IllegalStateException e) {
      updateBuilder.status(RecordCreationStatusEnum.ERROR).errorMessage(e.getMessage());
    }
    processDataImportEvent(data, updateBuilder.build());
  }

  @Override
  public void processDataImportError(DataImportEventPayload data) {
    log.debug("processDataImportError:: trying to process [{}] event for jobExecutionId [{}]",
      data.getEventType(), data.getJobExecutionId());
    var errorMessage = extractErrorMessage(data).orElse(ERROR_MISSED_MESSAGE);
    var updateBuilder = RecordCreationStatusUpdate.builder()
      .status(RecordCreationStatusEnum.ERROR)
      .errorMessage(errorMessage);
    processDataImportEvent(data, updateBuilder.build());
  }

  private void processDataImportEvent(DataImportEventPayload data, RecordCreationStatusUpdate statusUpdate) {
    var jobExecutionId = data.getJobExecutionId();
    log.info("Process [{}] event for jobExecutionId [{}]", data.getEventType(), jobExecutionId);
    var isUpdated = statusService.updateByJobExecutionId(jobExecutionId, statusUpdate);
    if (isUpdated) {
      log.info("Record creation status for jobExecutionId [{}] was updated with values [{}]", jobExecutionId,
        statusUpdate);
      var importResult = cacheService.getDataImportActionResult(jobExecutionId);
      if (importResult != null) {
        if (statusUpdate.getErrorMessage() == null) {
          importResult.setResult(ResponseEntity.noContent().build());
        } else {
          importResult.setErrorResult(buildErrorResponse(statusUpdate.getErrorMessage()));
        }
        cacheService.evictDataImportActionResult(jobExecutionId);
      }
    }
  }

  private ResponseEntity<Error> buildErrorResponse(String errorMessage) {
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, errorMessage);
    return ResponseEntity.badRequest().body(error);
  }
}
