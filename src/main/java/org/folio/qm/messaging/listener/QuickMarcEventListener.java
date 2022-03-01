package org.folio.qm.messaging.listener;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.folio.qm.util.DeferredResultCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.qm.service.CacheService;
import org.folio.qm.util.ErrorUtils;
import org.folio.tenant.domain.dto.Error;

@Log4j2
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class QuickMarcEventListener {

  public static final String QM_COMPLETED_LISTENER_ID = "quick-marc-qm-completed-listener";

  private final CacheService<DeferredResultCache> cacheService;
  private final ObjectMapper objectMapper;

  @KafkaListener(
    id = QM_COMPLETED_LISTENER_ID,
    groupId = "#{folioKafkaProperties.listener['qm-completed'].groupId}",
    topicPattern = "#{folioKafkaProperties.listener['qm-completed'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['qm-completed'].concurrency}",
    containerFactory = "quickMarcKafkaListenerContainerFactory")
  public void qmCompletedListener(QmCompletedEventPayload data) throws JsonProcessingException {
    DeferredResultCache updateCache = cacheService.getUpdateCache();
    var recordId = data.getRecordId();
    log.info("QM_COMPLETED received for record id [{}]", recordId);
    DeferredResult deferredResult = updateCache.getFromCache(String.valueOf(recordId));
    if (deferredResult != null) {
      if (data.isSucceed()) {
        deferredResult.setResult(ResponseEntity.accepted().build());
      } else {
        var errorMessage = data.getErrorMessage();
        if (isOptimisticLockingError(errorMessage)) {
          deferredResult.setErrorResult(buildOptimisticLockingErrorResponse(errorMessage));
        } else {
          ResponseEntity<Error> body = buildCommonErrorResponse(errorMessage);
          deferredResult.setErrorResult(body);
        }
      }
      updateCache.invalidate(String.valueOf(recordId));
    }
  }

  @NotNull
  private ResponseEntity<Error> buildCommonErrorResponse(String errorMessage) {
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, errorMessage);
    return ResponseEntity.badRequest().body(error);
  }

  @NotNull
  private ResponseEntity<Error> buildOptimisticLockingErrorResponse(String errorMessage) throws JsonProcessingException {
    var errorNode = objectMapper.readTree(errorMessage);
    var message = errorNode.get("message").asText();
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, message);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  private boolean isOptimisticLockingError(String errorMessage) {
    return errorMessage.contains("(optimistic locking)");
  }
}
