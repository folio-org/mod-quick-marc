package org.folio.qm.util;

import java.nio.charset.StandardCharsets;

import lombok.experimental.UtilityClass;
import org.apache.kafka.common.header.internals.RecordHeader;

@UtilityClass
public class KafkaUtils {

  public static final String JOB_EXECUTION_ID_HEADER = "jobExecutionId";
  public static final String CHUNK_ID_HEADER = "chunkId";
  public static final String CHUNK_NUMBER_HEADER = "chunkNumber";

  private static final String KAFKA_TOPIC_TEMPLATE = "%s.Default.%s.%s";

  public static String getTenantTopicName(String topicName, String tenantId, String envId) {
    return String.format(KAFKA_TOPIC_TEMPLATE, envId, tenantId, topicName);
  }

  public static RecordHeader header(String key, String value) {
    return new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8));
  }
}
