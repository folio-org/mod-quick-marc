package org.folio.qm.conveter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import static org.folio.qm.domain.dto.ParsedRecordDto.RecordTypeEnum.AUTHORITY;
import static org.folio.qm.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_COLLECTION_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_INVALID_008_LENGTH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_EDGE_CASES_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_EDGE_CASES_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getParsedRecordDtoWithMinContent;

import java.time.ZoneOffset;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.skyscreamer.jsonassert.JSONAssert;

import org.folio.qm.converter.impl.MarcAuthorityDtoConverter;
import org.folio.qm.domain.dto.MarcFieldProtectionSettingsCollection;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testentities.PhysicalDescriptionsTestEntities;

@UnitTest
class MarcAuthorityDtoConverterTest {

  private static final Logger logger = LogManager.getLogger(MarcAuthorityDtoConverterTest.class);

  @BeforeAll
  static void beforeAll() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @ParameterizedTest
  @EnumSource(value = PhysicalDescriptionsTestEntities.class, mode = EXCLUDE, names = {"ELECTRONIC_RESOURCE_MISSING_ITEMS"})
  void testSplitFixedLengthControlField(PhysicalDescriptionsTestEntities testEntity) throws JSONException {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.name());
    var parsedRecord = getMockAsObject(testEntity.getParsedRecordPath(), ParsedRecord.class);
    var parsedRecordDto = getParsedRecordDtoWithMinContent(parsedRecord, AUTHORITY);
    var converter = getConverter();
    var actual = converter.convert(parsedRecordDto);

    var expected = readQuickMarc(testEntity.getQuickMarcJsonPath());
    expected.setMarcFormat(MarcFormat.AUTHORITY);
    expected.setExternalHrid(null);
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(actual), true);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PARSED_RECORD_AUTHORITY_DTO_PATH + "," + QM_RECORD_AUTHORITY_PATH,
    PARSED_RECORD_AUTHORITY_EDGE_CASES_PATH + "," + QM_RECORD_AUTHORITY_EDGE_CASES_PATH,
  })
  void testParsedRecordToQuickMarcJsonConversion(String parsedRecordDtoPath, String quickMarcJsonPath) throws JSONException {
    logger.info("Testing Authority ParsedRecord -> QuickMarcJson conversion (expected flow + edge cases)");
    var converter = getConverter();
    ParsedRecordDto parsedRecordDto = getMockAsObject(parsedRecordDtoPath, ParsedRecordDto.class);
    parsedRecordDto.setRecordType(AUTHORITY);
    QuickMarc quickMarcJson = converter.convert(parsedRecordDto);
    var expected = readQuickMarc(quickMarcJsonPath);
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(quickMarcJson), true);
  }

  @Test
  void testAuthorityGeneralCharacteristicsControlFieldWrongLength() {
    logger.info("Testing Authority General Information wrong length after editing - ConverterException expected");
    var converter = getConverter();
    ParsedRecordDto parsedRecordDto = getMockAsObject(PARSED_RECORD_AUTHORITY_DTO_INVALID_008_LENGTH, ParsedRecordDto.class);
    parsedRecordDto.setRecordType(AUTHORITY);
    assertThrows(ConverterException.class, () -> converter.convert(parsedRecordDto));
  }

  private MarcAuthorityDtoConverter getConverter() {
    var settingsCollection =
      getMockAsObject(FIELD_PROTECTION_SETTINGS_COLLECTION_PATH, MarcFieldProtectionSettingsCollection.class);
    return new MarcAuthorityDtoConverter(settingsCollection);
  }
}
