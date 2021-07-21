package org.folio.qm.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import org.folio.qm.exception.ExternalException;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.qm.service.CacheService;
import org.folio.qm.util.ErrorUtils;

@Log4j2
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class QuickMarcEventListener {

  private final CacheService<DeferredResult> cacheService;

  @KafkaListener(groupId = "#{@groupIdProvider.qmCompletedGroupId()}",
    topicPattern = "#{@topicPatternProvider.qmCompletedTopicName()}",
    containerFactory = "quickMarcKafkaListenerContainerFactory")
  public void qmCompletedListener(QmCompletedEventPayload data, MessageHeaders messageHeaders) {
    var recordId = data.getRecordId();
    log.info("QM_COMPLETED received for record id [{}]", recordId);
    DeferredResult deferredResult = cacheService.getFromCache(recordId);
    if (deferredResult != null) {
      log.info("DeferredResult was found for record id [{}]", recordId);
      if (data.isSucceed()) {
        deferredResult.setResult(ResponseEntity.accepted().build());
      } else {
        var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, data.getErrorMessage());
        deferredResult.setErrorResult(new ExternalException(error));
      }
      cacheService.invalidate(recordId);
    }
  }
}
