package org.folio.qm.config;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.qm.domain.dto.DataImportEventPayload;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Bean
  public String kafkaEnvId(@Value("${ENV:folio}") String envId) {
    return envId;
  }

  @Bean
  public ConsumerFactory<String, DataImportEventPayload> dataImportConsumerFactory(KafkaProperties kafkaProperties,
                                                                                   Deserializer<DataImportEventPayload>
                                                                                     deserializer) {
    Map<String, Object> consumerProperties = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
    return new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, DataImportEventPayload>
    dataImportKafkaListenerContainerFactory(
    ConsumerFactory<String, DataImportEventPayload> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, DataImportEventPayload>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, QmCompletedEventPayload> quickMarcConsumerFactory(KafkaProperties kafkaProperties,
                                                                                   Deserializer<QmCompletedEventPayload>
                                                                                     deserializer) {
    Map<String, Object> consumerProperties = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
    return new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, QmCompletedEventPayload>
    quickMarcKafkaListenerContainerFactory(
    ConsumerFactory<String, QmCompletedEventPayload> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, QmCompletedEventPayload>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, SpecificationUpdatedEvent> specificationUpdatedConsumerFactory(
    KafkaProperties kafkaProperties, Deserializer<SpecificationUpdatedEvent> deserializer) {
    Map<String, Object> consumerProperties = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
    return new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SpecificationUpdatedEvent>
    specificationUpdatedKafkaListenerContainerFactory(
    ConsumerFactory<String, SpecificationUpdatedEvent> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, SpecificationUpdatedEvent>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }
}
