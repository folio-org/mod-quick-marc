package org.folio.qm.messaging.listener;

import static org.folio.qm.util.TenantContextUtils.getFolioExecutionContextFromQuickMarcEvent;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.qm.service.impl.DeferredResultCacheService;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.FolioModuleMetadata;
import org.folio.tenant.domain.dto.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class QuickMarcEventListener {

  public static final String QM_COMPLETED_LISTENER_ID = "quick-marc-qm-completed-listener";

  private final FolioModuleMetadata moduleMetadata;
  private final DeferredResultCacheService deferredResultCacheService;

  @KafkaListener(
    id = QM_COMPLETED_LISTENER_ID,
    groupId = "#{folioKafkaProperties.listener['qm-completed'].groupId}",
    topicPattern = "#{folioKafkaProperties.listener['qm-completed'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['qm-completed'].concurrency}",
    containerFactory = "quickMarcKafkaListenerContainerFactory")
  public void qmCompletedListener(QmCompletedEventPayload data, MessageHeaders headers) {
    runInFolioContext(getFolioExecutionContextFromQuickMarcEvent(headers, moduleMetadata),
      () -> processEvent(data));
  }

  private void processEvent(QmCompletedEventPayload data) {
    var recordId = data.getRecordId();
    log.info("Process [QM_COMPLETED] event for recordId [{}]", recordId);
    var deferredResult = deferredResultCacheService.getUpdateActionResult(recordId);
    if (deferredResult != null) {
      if (data.isSucceed()) {
        deferredResult.setResult(ResponseEntity.accepted().build());
      } else {
        var errorMessage = data.getErrorMessage();
        if (isOptimisticLockingError(errorMessage)) {
          deferredResult.setErrorResult(buildOptimisticLockingErrorResponse(errorMessage));
        } else {
          deferredResult.setErrorResult(buildCommonErrorResponse(errorMessage));
        }
      }
      deferredResultCacheService.evictUpdateActionResult(recordId);
    }
  }

  @NotNull
  private ResponseEntity<Error> buildCommonErrorResponse(String errorMessage) {
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, errorMessage);
    return ResponseEntity.badRequest().body(error);
  }

  @NotNull
  private ResponseEntity<Error> buildOptimisticLockingErrorResponse(String errorMessage) {
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, errorMessage);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  private boolean isOptimisticLockingError(String errorMessage) {
    return errorMessage.contains("(optimistic locking)");
  }
}
