package org.folio.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourcePathResolver {

  private ResourcePathResolver() {}

  public static final String CM_RECORDS = "change-manager.records";

  private static final Map<String, String> APIS;
  private static final Map<String, String> ITEM_APIS;

  static {
    Map<String, String> apis = new HashMap<>();
    apis.put(CM_RECORDS, "/change-manager/parsedRecords");
    APIS = Collections.unmodifiableMap(apis);
    ITEM_APIS = Collections.unmodifiableMap(APIS.entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue() + "/%s")));
  }

  public static String getResourcesPath(String field) {
    return APIS.get(field);
  }

  public static String getResourceByIdPath(String field, String id) {
    return String.format(ITEM_APIS.get(field), id);
  }
}
