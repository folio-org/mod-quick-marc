package org.folio.qm.messaging.deserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.support.utils.ApiTestUtils.TENANT_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.junit.jupiter.api.Test;

class SpecificationUpdatedEventDeserializerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
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
}
