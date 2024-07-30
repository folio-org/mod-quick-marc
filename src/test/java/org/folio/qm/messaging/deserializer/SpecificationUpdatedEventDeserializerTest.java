package org.folio.qm.messaging.deserializer;

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
  void deserialize_error() {
    var event = new SpecificationUpdatedEvent(UUID.randomUUID(), TENANT_ID);
    var serialized = objectMapper.writeValueAsBytes(event);
    when(objectMapper.readValue(any(byte[].class), eq(SpecificationUpdatedEvent.class)))
      .thenThrow(new IOException("test"));

    assertThrows(SerializationException.class, () -> deserializer.deserialize("test", serialized));
  }
}
