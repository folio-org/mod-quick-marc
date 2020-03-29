package org.folio.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourcePathResolver {

  private ResourcePathResolver() {}

  public static final String SRS_RECORDS = "source-records-storage.records";

  private static final Map<String, String> APIS;

  static {
    Map<String, String> apis = new HashMap<>();
    apis.put(SRS_RECORDS, "/source-storage/records");
    APIS = Collections.unmodifiableMap(apis);
  }

  public static String getResourcesPath(String field) {
    return APIS.get(field);
  }
}
