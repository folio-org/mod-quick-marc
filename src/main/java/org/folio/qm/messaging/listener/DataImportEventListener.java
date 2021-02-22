package org.folio.qm.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import org.folio.qm.service.DataImportEventProcessingService;
import org.folio.rest.jaxrs.model.DataImportEventPayload;

@Log4j2
@Component
@RequiredArgsConstructor
public class DataImportEventListener {

  private final DataImportEventProcessingService processingService;

  @KafkaListener(groupId = "#{@groupIdProvider.diCompletedGroupId()}",
    topicPattern = "#{@topicPatternProvider.diCompletedTopicName()}")
  public void diCompletedListener(DataImportEventPayload data, MessageHeaders messageHeaders) {
    processingService.processDICompleted(data);
  }

  @KafkaListener(groupId = "#{@groupIdProvider.diErrorGroupId()}",
    topicPattern = "#{@topicPatternProvider.diErrorTopicName()}")
  public void diErrorListener(DataImportEventPayload data, MessageHeaders messageHeaders) {
    processingService.processDIError(data);
  }
}
