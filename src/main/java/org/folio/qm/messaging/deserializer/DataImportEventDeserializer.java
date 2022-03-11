package org.folio.qm.messaging.deserializer;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.DataImportEventPayload;

@Component
@RequiredArgsConstructor
public class DataImportEventDeserializer implements Deserializer<DataImportEventPayload> {

  private final ObjectMapper objectMapper;

  @Override
  public DataImportEventPayload deserialize(String topic, byte[] data) {
    try {
      var eventPayload = objectMapper.readTree(data).get("eventPayload").asText();
      return objectMapper.readValue(eventPayload, DataImportEventPayload.class);
    } catch (IOException e) {
      throw new SerializationException("Can't deserialize data [" + Arrays.toString(data) +
        "] from topic [" + topic + "]", e);
    }
  }
}
