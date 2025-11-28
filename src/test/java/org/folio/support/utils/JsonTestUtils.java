package org.folio.support.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@UtilityClass
public class JsonTestUtils {

  private static final JsonMapper MAPPER = JsonMapper.builder()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .changeDefaultPropertyInclusion(value -> value.withValueInclusion(JsonInclude.Include.NON_NULL))
    .build();

  @SneakyThrows
  public static <T> T readQuickMarc(String filename, Class<T> quickMarcType) {
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
}
