package org.folio.qm.messaging.listener.providers;

public interface TopicPatternProvider {

  String diCompletedTopicName();

  String diErrorTopicName();

  String qmCompletedTopicName();
}
