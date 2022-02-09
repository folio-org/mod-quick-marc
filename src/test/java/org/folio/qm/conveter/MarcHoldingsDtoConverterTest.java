package org.folio.qm.conveter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import static org.folio.qm.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_INVALID_008_LENGTH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_EDGE_CASES_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS_EDGE_CASES_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_COLLECTION_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getParsedRecordDtoWithMinContent;

import java.time.ZoneOffset;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.skyscreamer.jsonassert.JSONAssert;

import org.folio.qm.converter.impl.MarcHoldingsDtoConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.utils.testentities.PhysicalDescriptionsTestEntities;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

class MarcHoldingsDtoConverterTest {

  private static final Logger logger = LogManager.getLogger(MarcHoldingsDtoConverterTest.class);

  @BeforeAll
  static void beforeAll() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @ParameterizedTest
  @EnumSource(value = PhysicalDescriptionsTestEntities.class, mode = EXCLUDE, names = {"ELECTRONIC_RESOURCE_MISSING_ITEMS"})
  void testSplitFixedLengthControlField(PhysicalDescriptionsTestEntities testEntity) throws JSONException {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.name());
    var parsedRecord = getMockAsObject(testEntity.getParsedRecordPath(), ParsedRecord.class);
    var parsedRecordDto = getParsedRecordDtoWithMinContent(parsedRecord, ParsedRecordDto.RecordType.MARC_HOLDING);
    var converter = getConverter();
    QuickMarc actual = converter.convert(parsedRecordDto);

    var expected = readQuickMarc(testEntity.getQuickMarcJsonPath());
    expected.setMarcFormat(MarcFormat.HOLDINGS);
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(actual), true);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PARSED_RECORD_HOLDINGS_DTO_PATH + "," + QM_RECORD_HOLDINGS_PATH,
    PARSED_RECORD_HOLDINGS_EDGE_CASES_PATH + "," + QM_RECORD_HOLDINGS_EDGE_CASES_PATH,
  })
  void testParsedRecordToQuickMarcJsonConversion(String parsedRecordDtoPath, String quickMarcJsonPath) throws JSONException {
    logger.info("Testing ParsedRecord -> QuickMarcJson conversion (expected flow + edge cases)");
    var converter = getConverter();
    ParsedRecordDto parsedRecordDto = getMockAsObject(parsedRecordDtoPath, ParsedRecordDto.class);
    parsedRecordDto.setRecordType(ParsedRecordDto.RecordType.MARC_HOLDING);
    QuickMarc quickMarcJson = converter.convert(parsedRecordDto);

    var expected = readQuickMarc(quickMarcJsonPath);
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(quickMarcJson), true);
  }

  @Test
  void testHoldingsGeneralCharacteristicsControlFieldWrongLength() {
    logger.info("Testing Holdings General Information wrong length after editing - ConverterException expected");
    var converter = getConverter();
    ParsedRecordDto parsedRecordDto = getMockAsObject(PARSED_RECORD_HOLDINGS_DTO_INVALID_008_LENGTH, ParsedRecordDto.class);
    parsedRecordDto.setRecordType(ParsedRecordDto.RecordType.MARC_HOLDING);
    assertThrows(ConverterException.class, () -> converter.convert(parsedRecordDto));
  }

  private MarcHoldingsDtoConverter getConverter(){
    MarcFieldProtectionSettingsCollection settingsCollection =
      getMockAsObject(FIELD_PROTECTION_SETTINGS_COLLECTION_PATH, MarcFieldProtectionSettingsCollection.class);
    return new MarcHoldingsDtoConverter(settingsCollection);
  }

}
