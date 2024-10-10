package org.folio.qm.controller;

import static org.apache.http.HttpStatus.SC_OK;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;
import static org.folio.qm.support.utils.ApiTestUtils.TENANT_ID;
import static org.folio.qm.support.utils.ApiTestUtils.mockGet;
import static org.folio.qm.support.utils.InputOutputTestUtils.readFile;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_VALIDATE_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.admin.NewTopic;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.testing.type.IntegrationTest;
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
    //mock specification retrieval
    mockGet("/specification-storage/specifications?family=MARC&include=all&limit=1&profile=bibliographic",
      readFile("mockdata/response/specifications/specification.json"), SC_OK, wireMockServer);
    //mock specification retrieval used for cache update
    mockGet("/specification-storage/specifications/6eefa4c6-bbf7-4845-ad82-de7fc4abd0e3?include=all",
      readFile("mockdata/response/specifications/specificationById.json"), SC_OK, wireMockServer);

    //validate record to have specification cached
    var validatableRecord = readQuickMarc(QM_RECORD_VALIDATE_PATH, ValidatableRecord.class);
    postResultActions("/records-editor/validate", validatableRecord, Map.of())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.issues.size()").value(2));

    //update specification in cache with additional rules
    var specificationEvent = new SpecificationUpdatedEvent(
      UUID.fromString("6eefa4c6-bbf7-4845-ad82-de7fc4abd0e3"), TENANT_ID,
      SpecificationUpdatedEvent.UpdateExtent.PARTIAL);
    sendSpecificationKafkaRecord(specificationEvent);

    //validate record and verify new cached specification was used
    await().atMost(FIVE_SECONDS).pollInterval(TWO_HUNDRED_MILLISECONDS).untilAsserted(() ->
      postResultActions("/records-editor/validate", validatableRecord, Map.of())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issues.size()").value(3)));
  }
}
