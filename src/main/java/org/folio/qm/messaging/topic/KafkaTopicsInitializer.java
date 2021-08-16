package org.folio.qm.messaging.topic;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import org.folio.qm.messaging.domain.QmEventTypes;
import org.folio.rest.jaxrs.model.DataImportEventTypes;
import org.folio.spring.FolioExecutionContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaTopicsInitializer {

  private final KafkaAdmin kafkaAdmin;
  private final BeanFactory beanFactory;
  private final FolioExecutionContext folioExecutionContext;
  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  private final String kafkaEnvId;

  public void createTopics() {
    if (folioExecutionContext == null) {
      throw new IllegalStateException("Could be executed on in Folio-request scope");
    }
    var tenantId = folioExecutionContext.getTenantId();
    var topicList = tenantSpecificTopics(tenantId);

    log.info("Creating topics for kafka [topics: {}]", topicList);
    var configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
    topicList.forEach(newTopic -> {
      var beanName = newTopic.name() + ".topic";
      if (!configurableBeanFactory.containsBean(beanName)) {
        configurableBeanFactory.registerSingleton(beanName, newTopic);
      }
    });
    kafkaAdmin.initialize();
    kafkaListenerEndpointRegistry.getListenerContainers().forEach(messageListenerContainer -> {
      messageListenerContainer.stop();
      messageListenerContainer.start();
    });
  }

  private List<NewTopic> tenantSpecificTopics(String tenant) {
    var eventsNameStreamBuilder = Stream.<Enum<?>>builder();
    for (QmEventTypes qmEventType : QmEventTypes.values()) {
      eventsNameStreamBuilder.add(qmEventType);
    }
    eventsNameStreamBuilder.add(DataImportEventTypes.DI_ERROR);
    eventsNameStreamBuilder.add(DataImportEventTypes.DI_COMPLETED);
    return eventsNameStreamBuilder.build()
      .map(Enum::name)
      .map(topic -> getTenantTopicName(topic, tenant))
      .map(this::toKafkaTopic)
      .collect(Collectors.toList());
  }

  private NewTopic toKafkaTopic(String topic) {
    return TopicBuilder.name(topic).build();
  }

  private String getTenantTopicName(String topicName, String tenantId) {
    return String.format("%s.Default.%s.%s", kafkaEnvId, tenantId, topicName);
  }
}
