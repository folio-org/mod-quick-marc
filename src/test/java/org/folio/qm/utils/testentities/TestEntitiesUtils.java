package org.folio.qm.utils.testentities;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.AdditionalInfo;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.Metadata;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.folio.rest.jaxrs.model.ParsedRecordDto.RecordType;

public class TestEntitiesUtils {

  public static final String QM_JSON_DIR = "mockdata/change-manager";
  public static final String RESTORED_PARSED_RECORD_AUTHORITY_DTO_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDtoRestored.json";
  public static final String RESTORED_PARSED_RECORD_BIB_DTO_PATH = QM_JSON_DIR + "/parsedRecordBibDtoRestored.json";
  public static final String RESTORED_PARSED_RECORD_HOLDINGS_DTO_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDtoRestored.json";
  public static final String PARSED_RECORD_AUTHORITY_EDGE_CASES_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDtoMissingWhitespaces.json";
  public static final String PARSED_RECORD_BIB_EDGE_CASES_PATH = QM_JSON_DIR + "/parsedRecordBibDtoMissingWhitespaces.json";
  public static final String PARSED_RECORD_HOLDINGS_EDGE_CASES_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDtoMissingWhitespaces.json";
  public static final String USER_JOHN_PATH = "mockdata/users/userJohn.json";
  public static final String PARSED_RECORD_BIB_DTO_PATH = QM_JSON_DIR + "/parsedRecordBibDto.json";
  public static final String PARSED_RECORD_HOLDINGS_DTO_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDto.json";
  public static final String PARSED_RECORD_HOLDINGS_DTO2_PATH = QM_JSON_DIR + "/parsedRecordHoldingsDto2.json";
  public static final String PARSED_RECORD_AUTHORITY_DTO_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDto.json";
  public static final String PARSED_RECORD_AUTHORITY_DTO2_PATH = QM_JSON_DIR + "/parsedRecordAuthorityDto2.json";
  public static final String PARSED_RECORD_AUTHORITY_DTO_INVALID_008_LENGTH = QM_JSON_DIR + "/parsedRecordAuthorityDto_invalid_008_field_length.json";
  public static final String PARSED_RECORD_HOLDINGS_DTO_INVALID_008_LENGTH = QM_JSON_DIR + "/parsedRecordHoldingsDto_invalid_008_field_length.json";
  public static final String PARSED_RECORDS_DIR = "mockdata/quick-marc-json";
  public static final String QM_EMPTY_FIELDS = PARSED_RECORDS_DIR + "/quickMarcJson_emptyContent.json";
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

  public static final String TESTED_TAG_NAME = "333";
  public static final String VALID_PARSED_RECORD_DTO_ID = "c56b70ce-4ef6-47ef-8bc3-c470bafa0b8c";
  public static final String EXISTED_EXTERNAL_ID = "b9a5f035-de63-4e2c-92c2-07240c89b817";
  public static final String EXISTED_EXTERNAL_HRID = "hold0001";
  public static final String VALID_PARSED_RECORD_ID = "c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1";
  public static final String VALID_JOB_EXECUTION_ID = "a7fb1c32-1ffb-4a22-a76a-4067284fe68d";
  public static final UUID JOB_EXECUTION_ID = UUID.fromString(VALID_JOB_EXECUTION_ID);
  public static final String JOHN_USER_ID = "38d3a441-c100-5e8d-bd12-71bde492b723";
  public static final String JOHN_USER_NAME = "john-doe";

  public static FieldItem getFieldWithIndicators(List<String> indicators) {
    return new FieldItem().tag(TESTED_TAG_NAME).content("$333 content").indicators(indicators);
  }

  public static FieldItem getFieldWithValue(String tag, String value) {
    return new FieldItem().tag(tag).content(value).indicators(List.of(" ", " "));
  }

  public static QuickMarc getQuickMarcJsonWithMinContent(FieldItem... fields) {
    return new QuickMarc().leader("01542ccm a2200361   4500").fields(Arrays.asList(fields));
  }

  public static ParsedRecordDto getParsedRecordDtoWithMinContent(ParsedRecord parsedRecord, RecordType recordType) {
    var parsedRecordDto = new ParsedRecordDto()
      .withId(VALID_PARSED_RECORD_DTO_ID)
      .withParsedRecord(parsedRecord)
      .withAdditionalInfo(new AdditionalInfo().withSuppressDiscovery(false))
      .withRecordType(recordType)
      .withRecordState(ParsedRecordDto.RecordState.ACTUAL)
      .withMetadata(new Metadata()
        .withUpdatedDate(new Date(1594901616879L))
        .withUpdatedByUserId(JOHN_USER_ID)
        .withUpdatedByUsername(JOHN_USER_NAME)
      );
    if (RecordType.MARC_BIB == recordType) {
      return parsedRecordDto
        .withExternalIdsHolder(new ExternalIdsHolder().withInstanceId(EXISTED_EXTERNAL_ID).withInstanceHrid("393893"));
    } else if (RecordType.MARC_HOLDING == recordType) {
      return parsedRecordDto
        .withExternalIdsHolder(new ExternalIdsHolder().withHoldingsId(EXISTED_EXTERNAL_ID).withHoldingsHrid("393893"));
    } else if (RecordType.MARC_AUTHORITY == recordType)
    {
      return parsedRecordDto.withExternalIdsHolder(null);
    }
    return parsedRecordDto;
  }
}
