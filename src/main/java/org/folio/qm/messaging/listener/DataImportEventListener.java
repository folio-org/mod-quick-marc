package org.folio.qm.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.DataImportEventPayload;
import org.folio.qm.service.EventProcessingService;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportEventListener {

  public static final String DI_COMPLETED_LISTENER_ID = "quick-marc-di-completed-listener";
  public static final String DI_ERROR_LISTENER_ID = "quick-marc-di-error-listener";

  private final EventProcessingService eventProcessingService;

  @KafkaListener(
    id = DI_COMPLETED_LISTENER_ID,
    groupId = "#{folioKafkaProperties.listener['di-completed'].groupId}",
    topicPattern = "#{folioKafkaProperties.listener['di-completed'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['di-completed'].concurrency}",
    containerFactory = "dataImportKafkaListenerContainerFactory")
  public void diCompletedListener(DataImportEventPayload data, MessageHeaders messageHeaders) {
    eventProcessingService.processDICompleted(data);
  }

  @KafkaListener(
    id = DI_ERROR_LISTENER_ID,
    groupId = "#{folioKafkaProperties.listener['di-error'].groupId}",
    topicPattern = "#{folioKafkaProperties.listener['di-error'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['di-error'].concurrency}",
    containerFactory = "dataImportKafkaListenerContainerFactory")
  public void diErrorListener(DataImportEventPayload data, MessageHeaders messageHeaders) {
    eventProcessingService.processDIError(data);
  }
}
