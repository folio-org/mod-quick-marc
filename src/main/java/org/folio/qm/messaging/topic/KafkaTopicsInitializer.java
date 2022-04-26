package org.folio.qm.messaging.topic;

import static org.folio.qm.util.KafkaUtils.getTenantTopicName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import org.folio.qm.config.properties.FolioEnvironment;
import org.folio.qm.config.properties.FolioKafkaProperties;
import org.folio.qm.messaging.domain.DIEventTypes;
import org.folio.spring.FolioExecutionContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaTopicsInitializer {

  private final KafkaAdmin kafkaAdmin;
  private final BeanFactory beanFactory;
  private final FolioExecutionContext folioExecutionContext;
  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  private final FolioKafkaProperties folioKafkaProperties;
  private final FolioEnvironment environment;

  public void createTopics() {
    if (folioExecutionContext == null) {
      throw new IllegalStateException("Could be executed only in Folio-request scope");
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
  }

  public void restartEventListeners() {
    kafkaListenerEndpointRegistry.getAllListenerContainers().forEach(container -> {
        log.info("Restarting kafka consumer to start listening created topics [ids: {}]", container.getListenerId());
        container.stop();
        container.start();
      }
    );
  }

  private List<NewTopic> tenantSpecificTopics(String tenant) {
    return Arrays.stream(DIEventTypes.values())
      .map(Enum::name)
      .map(topic -> getTenantTopicName(topic, tenant, environment.getEnvId()))
      .map(this::toKafkaTopic)
      .collect(Collectors.toList());
  }

  private NewTopic toKafkaTopic(String topic) {
    return TopicBuilder.name(topic)
      .replicas(folioKafkaProperties.getReplicationFactor())
      .partitions(folioKafkaProperties.getNumberOfPartitions())
      .build();
  }
}
