package org.folio.qm.messaging.listener;

import static org.folio.qm.util.TenantContextUtils.getFolioExecutionContextFromSpecification;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.service.MarcSpecificationService;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SpecificationEventListener {

  public static final String SPECIFICATION_UPDATED_LISTENER_ID = "quick-marc-specification-updated-listener";

  private final FolioModuleMetadata moduleMetadata;
  private final MarcSpecificationService specificationService;

  @KafkaListener(
    id = SPECIFICATION_UPDATED_LISTENER_ID,
    groupId = "#{folioKafkaProperties.listener['specification-updated'].groupId}",
    topicPattern = "#{folioKafkaProperties.listener['specification-updated'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['specification-updated'].concurrency}",
    containerFactory = "specificationUpdatedKafkaListenerContainerFactory")
  public void specificationUpdatedListener(SpecificationUpdatedEvent event, MessageHeaders messageHeaders) {
    runInFolioContext(getFolioExecutionContextFromSpecification(messageHeaders, event.tenantId(), moduleMetadata),
      () -> specificationService.updateSpecificationCache(event));
  }
}
