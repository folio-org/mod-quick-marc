package org.folio.it.kafka;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;
import static org.folio.support.utils.ApiTestUtils.TENANT_ID;
import static org.folio.support.utils.ApiTestUtils.recordsEditorValidatePath;
import static org.folio.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VALIDATE_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import org.apache.kafka.clients.admin.NewTopic;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.kafka.core.KafkaAdmin;

@IntegrationTest
class SpecificationKafkaListenerIT extends BaseIT {
  @Autowired
  private KafkaAdmin kafkaAdmin;
  @Autowired
  private BeanFactory beanFactory;
  @Autowired
  private KafkaTopicsInitializer topicsInitializer;

  @PostConstruct
  public void setUpKafka() {
    var topic = new NewTopic(SPECIFICATION_COMPLETE_TOPIC_NAME, 1, (short) 1);
    var beanName = topic.name() + ".topic";
    var configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
    if (!configurableBeanFactory.containsBean(beanName)) {
      configurableBeanFactory.registerSingleton(beanName, topic);
      kafkaAdmin.initialize();
      topicsInitializer.restartEventListeners();
    }
  }

  @Test
  void specificationUpdatedEvent() throws Exception {
    //validate record to have specification cached
    var validatableRecord = readQuickMarc(QM_RECORD_VALIDATE_PATH, ValidatableRecord.class);
    doPost(recordsEditorValidatePath(), validatableRecord)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.issues.size()").value(2));

    //update specification in cache with additional rules
    var specificationEvent = new SpecificationUpdatedEvent(
      UUID.fromString("6eefa4c6-bbf7-4845-ad82-de7fc4abd0e3"), TENANT_ID, Family.MARC, FamilyProfile.BIBLIOGRAPHIC,
      SpecificationUpdatedEvent.UpdateExtent.PARTIAL);
    sendSpecificationKafkaRecord(specificationEvent);

    //validate record and verify new cached specification was used
    await().atMost(FIVE_SECONDS).pollInterval(TWO_HUNDRED_MILLISECONDS).untilAsserted(() ->
      doPost(recordsEditorValidatePath(), validatableRecord)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issues.size()").value(3)));
  }
}
