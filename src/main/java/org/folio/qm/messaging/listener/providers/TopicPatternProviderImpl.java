package org.folio.qm.messaging.listener.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.folio.qm.messaging.domain.QmEventTypes;
import org.folio.rest.jaxrs.model.DataImportEventTypes;

@Component("topicPatternProvider")
@RequiredArgsConstructor
public class TopicPatternProviderImpl implements TopicPatternProvider {

  private static final String TOPIC_PATTERN_TEMPLATE = "%s\\.[a-zA-z0-9-]+\\.\\w+\\.%s";

  private final String kafkaEnvId;

  @Override
  public String diCompletedTopicName() {
    return createTopicPattern(DataImportEventTypes.DI_COMPLETED.name());
  }

  @Override
  public String diErrorTopicName() {
    return createTopicPattern(DataImportEventTypes.DI_ERROR.name());
  }

  @Override
  public String qmCompletedTopicName() {
    return createTopicPattern(QmEventTypes.QM_COMPLETED.name());
  }

  private String createTopicPattern(String eventName) {
    return String.format(TOPIC_PATTERN_TEMPLATE, kafkaEnvId, eventName);
  }
}
