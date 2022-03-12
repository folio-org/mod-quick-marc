package org.folio.qm.service.impl;

import static org.folio.qm.util.DIEventUtils.extractErrorMessage;
import static org.folio.qm.util.DIEventUtils.extractExternalId;
import static org.folio.qm.util.DIEventUtils.extractMarcId;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.DataImportEventPayload;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.qm.domain.entity.RecordCreationStatusUpdate;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.EventProcessingService;
import org.folio.qm.util.ErrorUtils;
import org.folio.tenant.domain.dto.Error;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportEventProcessingServiceImpl implements EventProcessingService {

  private static final String INSTANCE_ID_MISSED_MESSAGE = "Instance ID is missed in event payload";
  private static final String ERROR_MISSED_MESSAGE = "Error message is missed in event payload";

  private final CreationStatusService statusService;
  private final ObjectMapper objectMapper;
  private final DeferredResultCacheService cacheService;

  @Override
  public void processDICompleted(DataImportEventPayload data) {
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
    var isUpdated = statusService.updateByJobExecutionId(jobExecutionId, statusUpdate);
    if (isUpdated) {
      log.info("Record creation status for jobExecutionId [{}] was updated with values [{}]", jobExecutionId, statusUpdate);
      var importResult = cacheService.getDataImportActionResult(jobExecutionId);
      if (importResult != null) {
        if (statusUpdate.getErrorMessage() == null) {
          importResult.setResult(ResponseEntity.noContent().build());
        } else {
          importResult.setErrorResult(buildErrorResponse(statusUpdate.getErrorMessage()));
        }
      }
    }
  }

  private ResponseEntity<Error> buildErrorResponse(String errorMessage) {
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, errorMessage);
    return ResponseEntity.badRequest().body(error);
  }
}
