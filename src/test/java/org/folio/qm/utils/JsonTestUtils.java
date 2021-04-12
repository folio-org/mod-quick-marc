package org.folio.qm.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import org.folio.qm.domain.dto.QuickMarc;

@UtilityClass
public class JsonTestUtils {

  private static final JsonMapper MAPPER = JsonMapper.builder()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .serializationInclusion(JsonInclude.Include.NON_NULL)
    .addModule(new JavaTimeModule())
    .build();

  @SneakyThrows
  public static QuickMarc readQuickMarc(String filename) {
    return MAPPER.readValue(IOTestUtils.readFile(filename), QuickMarc.class);
  }

  @SneakyThrows
  public static <T> T getObjectFromJson(String json, Class<T> tClass) {
    return MAPPER.readValue(json, tClass);
  }

  @SneakyThrows
  public static <T> T getObjectFromJsonNode(JsonNode json, Class<T> tClass) {
    return MAPPER.treeToValue(json, tClass);
  }

  @SneakyThrows
  public static <T> T getMockAsObject(String mockPath, Class<T> tClass) {
    return getObjectFromJson(IOTestUtils.readFile(mockPath), tClass);
  }

  @SneakyThrows
  public static ObjectNode getMockAsJsonNode(String fullPath) {
    return ((ObjectNode) MAPPER.readTree(IOTestUtils.readFile(fullPath)));
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
