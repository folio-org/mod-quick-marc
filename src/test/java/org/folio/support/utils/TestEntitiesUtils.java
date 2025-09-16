package org.folio.support.utils;

import java.util.List;
import org.folio.qm.domain.dto.FieldItem;

public class TestEntitiesUtils {

  public static final String SAMPLES_DIR = "__files";
  public static final String RECORDS_EDITOR_DIR = SAMPLES_DIR + "/records-editor";
  public static final String QM_RECORD_EDIT_BIB_PATH = RECORDS_EDITOR_DIR + "/quickMarcBibEdit.json";
  public static final String QM_RECORD_CREATE_BIB_PATH = RECORDS_EDITOR_DIR + "/quickMarcBibCreate.json";
  public static final String QM_RECORD_VALIDATE_PATH = RECORDS_EDITOR_DIR + "/quickMarcValidate.json";
  public static final String QM_RECORD_VALIDATE_SUBFIELD_PATH = RECORDS_EDITOR_DIR + "/quickMarcSubfieldValidate.json";
  public static final String QM_RECORD_VIEW_BIB_PATH = RECORDS_EDITOR_DIR + "/quickMarcBibView.json";
  public static final String QM_RECORD_EDIT_HOLDINGS_PATH = RECORDS_EDITOR_DIR + "/quickMarcHoldingsEdit.json";
  public static final String QM_RECORD_CREATE_HOLDINGS_PATH = RECORDS_EDITOR_DIR + "/quickMarcHoldingsCreate.json";
  public static final String QM_RECORD_VIEW_HOLDINGS_PATH = RECORDS_EDITOR_DIR + "/quickMarcHoldingsView.json";
  public static final String QM_RECORD_CREATE_AUTHORITY_PATH = RECORDS_EDITOR_DIR + "/quickMarcAuthorityCreate.json";
  public static final String QM_RECORD_EDIT_AUTHORITY_PATH = RECORDS_EDITOR_DIR + "/quickMarcAuthorityEdit.json";
  public static final String QM_RECORD_VIEW_AUTHORITY_PATH = RECORDS_EDITOR_DIR + "/quickMarcAuthorityView.json";
  public static final String SOURCE_RECORDS_DIR = SAMPLES_DIR + "/source-records";
  public static final String SOURCE_RECORD_BIB_PATH = SOURCE_RECORDS_DIR + "/bibliographic.json";
  public static final String SOURCE_RECORD_HOLDINGS_PATH = SOURCE_RECORDS_DIR + "/holdings.json";
  public static final String SOURCE_RECORD_AUTHORITY_PATH = SOURCE_RECORDS_DIR + "/authority.json";
  public static final String DI_EVENT_DIR = SAMPLES_DIR + "/di-event";
  public static final String DI_EVENT_WITH_INSTANCE = DI_EVENT_DIR + "/complete-event-with-instance.json";
  public static final String DI_EVENT_WITH_AUTHORITY = DI_EVENT_DIR + "/complete-event-with-authority.json";
  public static final String DI_EVENT_WITH_HOLDINGS = DI_EVENT_DIR + "/complete-event-with-holdings.json";
  public static final String DI_EVENT_WITHOUT_RECORD = DI_EVENT_DIR + "/complete-event-without-external-record.json";
  public static final String DI_EVENT_WITH_INVALID_JSON = DI_EVENT_DIR + "/complete-event-with-invalid-json.json";
  public static final String DI_ERROR_EVENT = DI_EVENT_DIR + "/error-event.json";

  public static final String JOB_EXECUTION_ID = "a7fb1c32-1ffb-4a22-a76a-4067284fe68d";
  public static final String JOHN_USER_ID = "38d3a441-c100-5e8d-bd12-71bde492b723";
  public static final String LINK_STATUS_ERROR = "ERROR";
  public static final String LINK_ERROR_CAUSE = "test";
  public static final String INSTANCE_ID = "00000000-0000-0000-0000-000000000001";
  public static final String HOLDINGS_ID = "00000000-0000-0000-0000-000000000002";
  public static final String AUTHORITY_ID = "00000000-0000-0000-0000-000000000003";

  public static FieldItem getFieldWithValue(String tag, String value) {
    return new FieldItem().tag(tag).content(value).indicators(List.of(" ", " "));
  }
}
