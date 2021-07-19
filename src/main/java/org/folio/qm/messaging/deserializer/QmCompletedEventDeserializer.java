package org.folio.qm.messaging.deserializer;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;

import org.folio.qm.messaging.domain.QmCompletedEventPayload;

@Component
@RequiredArgsConstructor
public class QmCompletedEventDeserializer implements Deserializer<QmCompletedEventPayload> {

  private final ObjectMapper objectMapper;

  @Override
  public QmCompletedEventPayload deserialize(String topic, byte[] data) {
    try {
      var eventPayload = objectMapper.readValue(data, QmCompletedEventPayload.class);
      if (StringUtils.isBlank(eventPayload.getErrorMessage())) {
        eventPayload.setSucceed(true);
      }
      return eventPayload;
    } catch (IOException e) {
      throw new SerializationException("Can't deserialize data [" + Arrays.toString(data) +
        "] from topic [" + topic + "]", e);
    }
  }
}
