package org.folio.qm.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.folio.rest.jaxrs.model.DataImportEventPayload;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Bean
  public String kafkaEnvId(@Value("${ENV:folio}") String envId) {
    return envId;
  }

  @Bean
  public ConsumerFactory<String, DataImportEventPayload> dataImportConsumerFactory(KafkaProperties kafkaProperties,
                                                                         Deserializer<DataImportEventPayload> deserializer) {
    var consumerProperties = kafkaProperties.buildConsumerProperties();
    return new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, DataImportEventPayload> dataImportKafkaListenerContainerFactory(
    ConsumerFactory<String, DataImportEventPayload> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, DataImportEventPayload>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, QmCompletedEventPayload> quickMarcConsumerFactory(KafkaProperties kafkaProperties,
                                                                                   Deserializer<QmCompletedEventPayload> deserializer) {
    var consumerProperties = kafkaProperties.buildConsumerProperties();
    return new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, QmCompletedEventPayload> quickMarcKafkaListenerContainerFactory(
    ConsumerFactory<String, QmCompletedEventPayload> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, QmCompletedEventPayload>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }
}
