package org.folio.qm.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaUtils {

  private static final String KAFKA_TOPIC_TEMPLATE = "%s.Default.%s.%s";

  public static String getTenantTopicName(String topicName, String tenantId, String envId) {
    return String.format(KAFKA_TOPIC_TEMPLATE, envId, tenantId, topicName);
  }

}
