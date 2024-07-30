package org.folio.qm.messaging.deserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.support.utils.ApiTestUtils.TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.kafka.common.errors.SerializationException;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.junit.jupiter.api.Test;

class SpecificationUpdatedEventDeserializerTest {

  private final ObjectMapper objectMapper = spy(ObjectMapper.class);
  private final SpecificationUpdatedEventDeserializer deserializer =
    new SpecificationUpdatedEventDeserializer(objectMapper);

  @Test
  @SneakyThrows
  void deserialize() {
    var expected = new SpecificationUpdatedEvent(UUID.randomUUID(), TENANT_ID);
    var serialized = objectMapper.writeValueAsBytes(expected);

    var actual = deserializer.deserialize("test", serialized);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void deserialize_error() {
    var topic = "topic";
    var event = new SpecificationUpdatedEvent(UUID.randomUUID(), TENANT_ID);
    var serialized = objectMapper.writeValueAsBytes(event);
    when(objectMapper.readValue(any(byte[].class), eq(SpecificationUpdatedEvent.class)))
      .thenThrow(new IOException("test"));

    var ex = assertThrows(SerializationException.class, () -> deserializer.deserialize(topic, serialized));
    assertThat(ex.getMessage()).contains(topic, "Can't deserialize data");
  }
}
