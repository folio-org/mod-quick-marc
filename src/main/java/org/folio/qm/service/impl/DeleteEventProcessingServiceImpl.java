package org.folio.qm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.service.CacheService;
import org.folio.qm.service.EventProcessingService;
import org.folio.qm.util.DeferredResultCache;
import org.folio.qm.util.ErrorUtils;
import org.folio.rest.jaxrs.model.DataImportEventPayload;
import org.folio.tenant.domain.dto.Error;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import static org.folio.qm.util.DIEventUtils.extractErrorMessage;

@Log4j2
@Component(value = "deleteEventProcessingService")
@RequiredArgsConstructor
public class DeleteEventProcessingServiceImpl implements EventProcessingService {
  private static final String ERROR_MISSED_MESSAGE = "Error message is missed in event payload";

  private final CacheService<DeferredResultCache> cacheService;

  @Override
  public boolean processDICompleted(DataImportEventPayload data) {
    var result = false;
    var jobExecutionId = data.getJobExecutionId();
    DeferredResultCache deleteCache = cacheService.getDeleteCache();
    DeferredResult deferredResult = deleteCache.getFromCache(jobExecutionId);
    if (deferredResult != null) {
      log.info("Process [{}] event for jobExecutionId [{}]", data.getEventType(), jobExecutionId);
      deferredResult.setResult(ResponseEntity.noContent().build());
      deleteCache.invalidate(String.valueOf(jobExecutionId));
      result = true;
    }
    return result;
  }

  @Override
  public boolean processDIError(DataImportEventPayload data) {
    var result = false;
    var jobExecutionId = data.getJobExecutionId();
    DeferredResultCache deleteCache = cacheService.getDeleteCache();
    DeferredResult deferredResult = deleteCache.getFromCache(jobExecutionId);
    if (deferredResult != null) {
      log.info("Process [{}] event for jobExecutionId [{}]", data.getEventType(), jobExecutionId);
      var errorMessage = extractErrorMessage(data).orElse(ERROR_MISSED_MESSAGE);
      ResponseEntity<Error> body = buildCommonErrorResponse(errorMessage);
      deferredResult.setErrorResult(body);
      deleteCache.invalidate(jobExecutionId);
      result = true;
    }

    return result;
  }

  private ResponseEntity<Error> buildCommonErrorResponse(String errorMessage) {
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, errorMessage);
    return ResponseEntity.badRequest().body(error);
  }
}
