package org.folio.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourcePathResolver {

  private ResourcePathResolver() {}

  public static final String SRS_RECORDS = "source-records-storage.records";
  public static final String CHANGE_MANAGER = "change-manager.parsedRecords";

  private static final Map<String, String> APIS;
  private static final Map<String, String> ITEM_APIS;

  static {
    Map<String, String> apis = new HashMap<>();
    apis.put(SRS_RECORDS, "/source-storage/records");
    apis.put(CHANGE_MANAGER, "/change-manager/parsedRecords");
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
