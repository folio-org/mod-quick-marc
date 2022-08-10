package org.folio.qm.messaging.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.folio.qm.messaging.domain.QmCompletedEventPayload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QmCompletedEventDeserializer implements Deserializer<QmCompletedEventPayload> {

  private final ObjectMapper objectMapper;

  @Override
  public QmCompletedEventPayload deserialize(String topic, byte[] data) {
    try {
      var eventPayload = objectMapper.readTree(data).get("eventPayload").asText();
      var qmCompletedEventPayload = objectMapper.readValue(eventPayload, QmCompletedEventPayload.class);
      if (StringUtils.isBlank(qmCompletedEventPayload.getErrorMessage())) {
        qmCompletedEventPayload.setSucceed(true);
      }
      return qmCompletedEventPayload;
    } catch (IOException e) {
      throw new SerializationException(
        "Can't deserialize data [" + Arrays.toString(data) + "] from topic [" + topic + "]", e);
    }
  }
}
