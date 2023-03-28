package org.folio.qm.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheNames {

  public static final String JOB_PROFILE_CACHE = "job-profiles";
  public static final String QM_UPDATE_RESULT_CACHE = "qm-update-results";
  public static final String QM_FETCH_LINKING_RULES_RESULTS = "qm-fetch-linking-rules-results";
  public static final String DATA_IMPORT_RESULT_CACHE = "data-import-results";
}
