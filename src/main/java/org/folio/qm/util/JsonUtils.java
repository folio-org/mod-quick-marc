package org.folio.qm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class JsonUtils {

  public static final String OBJECT_SERIALIZATION_FAILED = "Failed to serialize object to a json string ";
  public static final String OBJECT_DESERIALIZATION_FAILED = "Failed to deserialize json string to an object ";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String objectToJsonString(Object o) {
    String jsonString;
    try {
      jsonString = OBJECT_MAPPER.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      log.info(OBJECT_SERIALIZATION_FAILED, e);
      throw new IllegalStateException(OBJECT_SERIALIZATION_FAILED + e.getMessage());
    }
    return jsonString;
  }

  public static <T> T jsonToObject(String jsonString, Class<T> valueType) {
    T obj;
    try {
      obj = OBJECT_MAPPER.readValue(jsonString, valueType);
    } catch (JsonProcessingException e) {
      log.info(OBJECT_DESERIALIZATION_FAILED, e);
      throw new IllegalStateException(OBJECT_DESERIALIZATION_FAILED + e.getMessage());
    }
    return obj;
  }
}
