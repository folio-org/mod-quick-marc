package org.folio.qm.messaging.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpecificationUpdatedEventDeserializer implements Deserializer<SpecificationUpdatedEvent> {

  private final ObjectMapper objectMapper;

  @Override
  public SpecificationUpdatedEvent deserialize(String topic, byte[] data) {
    try {
      return objectMapper.readValue(data, SpecificationUpdatedEvent.class);
    } catch (IOException e) {
      throw new SerializationException(
        "Can't deserialize data [" + Arrays.toString(data) + "] from topic [" + topic + "]", e);
    }
  }
}
