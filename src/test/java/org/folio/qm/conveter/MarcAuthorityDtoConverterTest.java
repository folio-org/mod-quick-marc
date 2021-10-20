package org.folio.qm.conveter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import static org.folio.qm.utils.AssertionUtils.mockIsEqualToObject;
import static org.folio.qm.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_INVALID_008_LENGTH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_EDGE_CASES_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_EDGE_CASES_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getParsedRecordDtoWithMinContent;

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
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.utils.testentities.PhysicalDescriptionsTestEntities;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

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
    var parsedRecordDto = getParsedRecordDtoWithMinContent(parsedRecord, ParsedRecordDto.RecordType.MARC_AUTHORITY);
    var converter = new MarcAuthorityDtoConverter();
    var actual = converter.convert(parsedRecordDto);

    var expected = readQuickMarc(testEntity.getQuickMarcJsonPath());
    expected.setMarcFormat(MarcFormat.AUTHORITY);
    expected.setExternalHrid(null);
    expected.setExternalId(null);
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(actual), true);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PARSED_RECORD_AUTHORITY_DTO_PATH + "," + QM_RECORD_AUTHORITY_PATH,
    PARSED_RECORD_AUTHORITY_EDGE_CASES_PATH + "," + QM_RECORD_AUTHORITY_EDGE_CASES_PATH,
  })
  void testParsedRecordToQuickMarcJsonConversion(String parsedRecordDtoPath, String quickMarcJsonPath) {
    logger.info("Testing Authority ParsedRecord -> QuickMarcJson conversion (expected flow + edge cases)");
    var converter = new MarcAuthorityDtoConverter();
    ParsedRecordDto parsedRecordDto = getMockAsObject(parsedRecordDtoPath, ParsedRecordDto.class);
    parsedRecordDto.setRecordType(ParsedRecordDto.RecordType.MARC_AUTHORITY);
    QuickMarc quickMarcJson = converter.convert(parsedRecordDto);
    mockIsEqualToObject(quickMarcJsonPath, quickMarcJson);
  }

  @Test
  void testAuthorityGeneralCharacteristicsControlFieldWrongLength() {
    logger.info("Testing Authority General Information wrong length after editing - ConverterException expected");
    var converter = new MarcAuthorityDtoConverter();
    ParsedRecordDto parsedRecordDto = getMockAsObject(PARSED_RECORD_AUTHORITY_DTO_INVALID_008_LENGTH, ParsedRecordDto.class);
    parsedRecordDto.setRecordType(ParsedRecordDto.RecordType.MARC_AUTHORITY);
    assertThrows(ConverterException.class, () -> converter.convert(parsedRecordDto));
  }
}
