package org.folio.it.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.messaging.listener.QuickMarcEventListener.QM_COMPLETED_LISTENER_ID;

import java.util.regex.Pattern;
import org.folio.it.BaseIT;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

@IntegrationTest
class QuickMarcEventListenerIT extends BaseIT {

  @Autowired
  private KafkaListenerEndpointRegistry listenerEndpointRegistry;

  @Test
  void testListenerInitialization() {
    var listenerContainer = listenerEndpointRegistry.getListenerContainer(QM_COMPLETED_LISTENER_ID);
    assertThat(listenerContainer).isNotNull();
    assertThat(listenerContainer.getGroupId()).isEqualTo("folio-mod-quick-marc-qm-group");
    assertThat(listenerContainer.getContainerProperties().getTopicPattern())
      .isNotNull()
      .extracting(Pattern::pattern)
      .isEqualTo("(folio\\.)[a-zA-z0-9-]+\\.\\w+\\.QM_COMPLETED");
  }
}
