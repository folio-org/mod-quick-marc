package org.folio.qm.support.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.folio.qm.domain.dto.BaseMarcRecord;

@UtilityClass
public class JsonTestUtils {

  private static final JsonMapper MAPPER = JsonMapper.builder()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .serializationInclusion(JsonInclude.Include.NON_NULL)
    .addModule(new JavaTimeModule())
    .build();

  @SneakyThrows
  public static <T extends BaseMarcRecord> T readQuickMarc(String filename, Class<T> quickMarcType) {
    return MAPPER.readValue(InputOutputTestUtils.readFile(filename), quickMarcType);
  }

  @SneakyThrows
  public static <T> T getObjectFromJson(String json, Class<T> entityClass) {
    return MAPPER.readValue(json, entityClass);
  }

  @SneakyThrows
  public static <T> T getMockAsObject(String mockPath, Class<T> entityClass) {
    return getObjectFromJson(InputOutputTestUtils.readFile(mockPath), entityClass);
  }

  @SneakyThrows
  public static String getObjectAsJson(Object o) {
    return MAPPER.writeValueAsString(o);
  }

  @SneakyThrows
  public static ObjectNode getObjectAsJsonNode(Object o) {
    return MAPPER.valueToTree(o);
  }
}
