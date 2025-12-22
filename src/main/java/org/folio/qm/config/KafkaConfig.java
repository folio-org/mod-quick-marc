package org.folio.qm.config;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Bean
  public String kafkaEnvId(@Value("${ENV:folio}") String envId) {
    return envId;
  }

  @Bean
  public ConsumerFactory<String, SpecificationUpdatedEvent> specificationConsumerFactory(
    KafkaProperties kafkaProperties) {
    var deserializer = new JsonDeserializer<>(SpecificationUpdatedEvent.class, false);
    Map<String, Object> consumerProperties = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
    return new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SpecificationUpdatedEvent> specificationContainerFactory(
    ConsumerFactory<String, SpecificationUpdatedEvent> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, SpecificationUpdatedEvent>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }
}
