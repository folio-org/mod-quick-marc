package org.folio.qm.support.utils.testentities;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;

public class TestEntitiesUtils {

  public static final String QM_JSON_DIR = "mockdata/request/change-manager";
  public static final String JOB_DIR = QM_JSON_DIR + "/job-execution";
  public static final String JOB_EXECUTION_CREATED = JOB_DIR + "/jobExecutionCreated.json";
  public static final String USER_JOHN_PATH = "mockdata/request/users/userJohn.json";
  public static final String PARSED_RECORD_BIB_DTO_PATH = QM_JSON_DIR + "/parsedRecordBibDto.json";
  public static final String PARSED_RECORD_HOLDINGS_DTO_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDto.json";
  public static final String PARSED_RECORD_AUTHORITY_DTO_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDto.json";
  public static final String PARSED_RECORDS_DIR = "mockdata/responce";
  public static final String DI_EVENT_DIR = "mockdata/request/di-event";
  public static final String DI_COMPLETE_AUTHORITY_DELETE = DI_EVENT_DIR + "/complete-event-with-authority-delete.json";
  public static final String QM_RECORD_BIB_PATH = PARSED_RECORDS_DIR + "/quickMarcBib.json";
  public static final String QM_RECORD_HOLDINGS_PATH = PARSED_RECORDS_DIR + "/quickMarcHoldings.json";
  public static final String QM_RECORD_AUTHORITY_PATH = PARSED_RECORDS_DIR + "/quickMarcAuthority.json";
  public static final String PROTECTION_SETTINGS_PATH =
    "mockdata/request/change-manager/protection-settings";
  public static final String FIELD_PROTECTION_SETTINGS_PATH =
    PROTECTION_SETTINGS_PATH + "/fieldProtectionSettingsCollection.json";

  public static final String TESTED_TAG_NAME = "333";
  public static final UUID VALID_PARSED_RECORD_DTO_ID = UUID.fromString("c56b70ce-4ef6-47ef-8bc3-c470bafa0b8c");
  public static final UUID EXISTED_EXTERNAL_ID = UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c89b817");
  public static final String EXISTED_EXTERNAL_HRID = "hold0001";
  public static final UUID VALID_PARSED_RECORD_ID = UUID.fromString("c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1");
  public static final UUID VALID_JOB_EXECUTION_ID = UUID.fromString("a7fb1c32-1ffb-4a22-a76a-4067284fe68d");
  public static final String JOHN_USER_ID = "38d3a441-c100-5e8d-bd12-71bde492b723";

  public static FieldItem getFieldWithIndicators(List<String> indicators) {
    return new FieldItem().tag(TESTED_TAG_NAME).content("$333 content").indicators(indicators);
  }

  public static FieldItem getFieldWithValue(String tag, String value) {
    return new FieldItem().tag(tag).content(value).indicators(List.of(" ", " "));
  }

  public static QuickMarc getQuickMarcJsonWithMinContent(FieldItem... fields) {
    return new QuickMarc().leader("01542ccm a22002533  4500").fields(Arrays.asList(fields));
  }

}
