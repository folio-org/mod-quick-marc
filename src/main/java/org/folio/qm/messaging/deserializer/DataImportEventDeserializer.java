package org.folio.qm.messaging.deserializer;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.folio.qm.client.model.DataImportEventPayload;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class DataImportEventDeserializer implements Deserializer<DataImportEventPayload> {

  private final ObjectMapper objectMapper;

  @Override
  public DataImportEventPayload deserialize(String topic, byte[] data) {
    try {
      var eventPayload = objectMapper.readTree(data).get("eventPayload").asString();
      return objectMapper.readValue(eventPayload, DataImportEventPayload.class);
    } catch (JacksonException e) {
      throw new SerializationException(
        "Can't deserialize data [" + Arrays.toString(data) + "] from topic [" + topic + "]", e);
    }
  }
}
