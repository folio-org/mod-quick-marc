package org.folio.qm.support.utils.testentities;

import static org.folio.qm.domain.dto.ParsedRecordDto.RecordTypeEnum.AUTHORITY;
import static org.folio.qm.domain.dto.ParsedRecordDto.RecordTypeEnum.BIB;
import static org.folio.qm.domain.dto.ParsedRecordDto.RecordTypeEnum.HOLDING;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.folio.qm.domain.dto.AdditionalInfo;
import org.folio.qm.domain.dto.ExternalIdsHolder;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.Metadata;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;

public class TestEntitiesUtils {

  public static final String QM_JSON_DIR = "mockdata/change-manager";
  public static final String RESTORED_PARSED_RECORD_AUTHORITY_DTO_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDtoRestored.json";
  public static final String RESTORED_PARSED_RECORD_BIB_DTO_PATH = QM_JSON_DIR + "/parsedRecordBibDtoRestored.json";
  public static final String RESTORED_PARSED_RECORD_HOLDINGS_DTO_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDtoRestored.json";
  public static final String PARSED_RECORD_AUTHORITY_EDGE_CASES_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDtoMissingWhitespaces.json";
  public static final String PARSED_RECORD_BIB_EDGE_CASES_PATH = QM_JSON_DIR + "/parsedRecordBibDtoMissingWhitespaces.json";
  public static final String PARSED_RECORD_HOLDINGS_EDGE_CASES_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDtoMissingWhitespaces.json";
  public static final String JOB_DIR = QM_JSON_DIR + "/job-execution";
  public static final String JOB_EXECUTION_CREATED = JOB_DIR + "/jobExecutionCreated.json";
  public static final String USER_JOHN_PATH = "mockdata/users/userJohn.json";
  public static final String PARSED_RECORD_BIB_DTO_PATH = QM_JSON_DIR + "/parsedRecordBibDto.json";
  public static final String PARSED_RECORD_HOLDINGS_DTO_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDto.json";
  public static final String PARSED_RECORD_HOLDINGS_DTO2_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDto2.json";
  public static final String PARSED_RECORD_AUTHORITY_DTO_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDto.json";
  public static final String PARSED_RECORD_AUTHORITY_DTO2_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDto2.json";
  public static final String PARSED_RECORD_AUTHORITY_DTO_INVALID_008_LENGTH = QM_JSON_DIR + "/parsedRecordAuthorityDto_invalid_008_field_length.json";
  public static final String PARSED_RECORD_HOLDINGS_DTO_INVALID_008_LENGTH = QM_JSON_DIR + "/parsedRecordHoldingsDto_invalid_008_field_length.json";
  public static final String PARSED_RECORDS_PROTECTION_SETTINGS = "mockdata/parsed-records/protected-field/recordMarcProtectionSettings.json";
  public static final String PARSED_RECORDS_DIR = "mockdata/quick-marc-json";
  public static final String DI_EVENT_DIR = "mockdata/di-event";
  public static final String DI_COMPLETE_AUTHORITY = DI_EVENT_DIR + "/complete-event-with-autority.json";
  public static final String QM_WRONG_ITEM_LENGTH = PARSED_RECORDS_DIR + "/quickMarcJsonWrongItemLength.json";
  public static final String QM_LEADER_MISMATCH2 = PARSED_RECORDS_DIR + "/quickMarcJsonLeaderMismatchMissing008Value.json";
  public static final String QM_LEADER_MISMATCH1 = PARSED_RECORDS_DIR + "/quickMarcJsonLeaderMismatchValueMismatch.json";
  public static final String QM_RECORD_BIB_EDGE_CASES_PATH = PARSED_RECORDS_DIR + "/quickMarcBibMissingWhitespaces.json";
  public static final String QM_RECORD_HOLDINGS_EDGE_CASES_PATH = PARSED_RECORDS_DIR + "/quickMarcHoldingsMissingWhitespaces.json";
  public static final String QM_RECORD_AUTHORITY_EDGE_CASES_PATH = PARSED_RECORDS_DIR + "/quickMarcAuthorityMissingWhitespaces.json";
  public static final String QM_EDITED_RECORD_BIB_PATH = PARSED_RECORDS_DIR + "/quickMarcBib_edited.json";
  public static final String QM_EDITED_RECORD_HOLDINGS_PATH = PARSED_RECORDS_DIR + "/quickMarcHoldings_edited.json";
  public static final String QM_RECORD_HOLDINGS = PARSED_RECORDS_DIR + "/qiuckMarcHoldings.json";
  public static final String QM_RECORD_WITH_INCORRECT_TAG_PATH = PARSED_RECORDS_DIR + "/quickMarcJson_withIncorrectTag.json";
  public static final String QM_RECORD_BIB_PATH = PARSED_RECORDS_DIR + "/quickMarcBib.json";
  public static final String QM_RECORD_HOLDINGS_PATH = PARSED_RECORDS_DIR + "/quickMarcHoldings.json";
  public static final String QM_RECORD_AUTHORITY_PATH = PARSED_RECORDS_DIR + "/quickMarcAuthority.json";
  public static final String QM_EDITED_RECORD_AUTHORITY_PATH = PARSED_RECORDS_DIR + "/quickMarcAuthority_edited.json";
  public static final String QM_PROTECTION_SETTINGS = PARSED_RECORDS_DIR + "/protection_settings/quickMarcProtectionSettings.json";
  public static final String PROTECTION_SETTINGS_COLLECTION_PATH = "mockdata/protection-settings-collection";
  public static final String FIELD_PROTECTION_SETTINGS_COLLECTION_PATH = PROTECTION_SETTINGS_COLLECTION_PATH + "/fieldProtectionSettingsCollection.json";
  public static final String FIELD_PROTECTION_SETTINGS_COLLECTION_FULL_PATH = PROTECTION_SETTINGS_COLLECTION_PATH + "/fieldProtectionSettingsCollectionFull.json";

  public static final String TESTED_TAG_NAME = "333";
  public static final UUID VALID_PARSED_RECORD_DTO_ID = UUID.fromString("c56b70ce-4ef6-47ef-8bc3-c470bafa0b8c");
  public static final UUID EXISTED_EXTERNAL_ID = UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c89b817");
  public static final String EXISTED_EXTERNAL_HRID = "hold0001";
  public static final UUID VALID_PARSED_RECORD_ID = UUID.fromString("c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1");
  public static final UUID VALID_JOB_EXECUTION_ID = UUID.fromString("a7fb1c32-1ffb-4a22-a76a-4067284fe68d");
  public static final String JOHN_USER_ID = "38d3a441-c100-5e8d-bd12-71bde492b723";
  public static final String JOHN_USER_NAME = "john-doe";

  public static FieldItem getFieldWithIndicators(List<String> indicators) {
    return new FieldItem().tag(TESTED_TAG_NAME).content("$333 content").indicators(indicators);
  }

  public static FieldItem getFieldWithValue(String tag, String value) {
    return new FieldItem().tag(tag).content(value).indicators(List.of(" ", " "));
  }

  public static QuickMarc getQuickMarcJsonWithMinContent(FieldItem... fields) {
    return new QuickMarc().leader("01542ccm a22002533  4500").fields(Arrays.asList(fields));
  }

  public static ParsedRecordDto getParsedRecordDtoWithMinContent(ParsedRecord parsedRecord, ParsedRecordDto.RecordTypeEnum recordType) {
    var parsedRecordDto = new ParsedRecordDto()
      .id(VALID_PARSED_RECORD_DTO_ID)
      .parsedRecord(parsedRecord)
      .additionalInfo(new AdditionalInfo().suppressDiscovery(false))
      .recordType(recordType)
      .recordState(ParsedRecordDto.RecordStateEnum.ACTUAL)
      .metadata(new Metadata()
        .updatedDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(1594901616879L), ZoneId.systemDefault()))
        .updatedByUserId(JOHN_USER_ID)
        .updatedByUsername(JOHN_USER_NAME)
      );
    if (BIB == recordType) {
      return parsedRecordDto
        .externalIdsHolder(new ExternalIdsHolder().instanceId(EXISTED_EXTERNAL_ID).instanceHrid("393893"));
    } else if (HOLDING == recordType) {
      return parsedRecordDto
        .externalIdsHolder(new ExternalIdsHolder().holdingsId(EXISTED_EXTERNAL_ID).holdingsHrid("393893"));
    } else if (AUTHORITY == recordType) {
      return parsedRecordDto
        .externalIdsHolder(new ExternalIdsHolder().authorityId(EXISTED_EXTERNAL_ID));
    }
    return parsedRecordDto;
  }
}
