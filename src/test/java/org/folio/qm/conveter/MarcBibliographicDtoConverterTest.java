package org.folio.qm.conveter;

import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import static org.folio.qm.utils.AssertionUtils.mockIsEqualToObject;
import static org.folio.qm.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_EDGE_CASES_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_BIB_EDGE_CASES_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_BIB_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getParsedRecordDtoWithMinContent;

import java.time.ZoneOffset;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.skyscreamer.jsonassert.JSONAssert;

import org.folio.qm.converter.impl.MarcBibliographicDtoConverter;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.utils.testentities.GeneralTestEntities;
import org.folio.qm.utils.testentities.PhysicalDescriptionsTestEntities;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

class MarcBibliographicDtoConverterTest {

  private static final Logger logger = LogManager.getLogger(MarcBibliographicDtoConverterTest.class);

  @BeforeAll
  static void beforeAll() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @ParameterizedTest
  @EnumSource(value = GeneralTestEntities.class, mode = EXCLUDE, names = {"BOOKS_MISSING_ITEMS"})
  void testSplitFixedLengthControlField(GeneralTestEntities testEntity) throws JSONException {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.getMaterialTypeConfiguration().getName());
    ParsedRecord parsedRecord = getMockAsObject(testEntity.getParsedRecordPath(), ParsedRecord.class);
    MarcBibliographicDtoConverter converter = new MarcBibliographicDtoConverter();
    QuickMarc actual = converter.convert(getParsedRecordDtoWithMinContent(parsedRecord,
      ParsedRecordDto.RecordType.MARC_BIB));
    var expected = readQuickMarc(testEntity.getQuickMarcJsonPath());
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(actual), true);
  }

  @ParameterizedTest
  @EnumSource(value = PhysicalDescriptionsTestEntities.class, mode = EXCLUDE, names = {"ELECTRONIC_RESOURCE_MISSING_ITEMS"})
  void testSplitFixedLengthControlField(PhysicalDescriptionsTestEntities testEntity) throws JSONException {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.name());
    ParsedRecord parsedRecord = getMockAsObject(testEntity.getParsedRecordPath(), ParsedRecord.class);
    MarcBibliographicDtoConverter converter = new MarcBibliographicDtoConverter();
    QuickMarc quickMarcJson = converter.convert(getParsedRecordDtoWithMinContent(parsedRecord,
      ParsedRecordDto.RecordType.MARC_BIB));
    var expected = readQuickMarc(testEntity.getQuickMarcJsonPath());
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(quickMarcJson), true);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PARSED_RECORD_BIB_DTO_PATH + "," + QM_RECORD_BIB_PATH,
    PARSED_RECORD_BIB_EDGE_CASES_PATH + "," + QM_RECORD_BIB_EDGE_CASES_PATH,
  })
  void testParsedRecordToQuickMarcJsonConversion(String parsedRecordDtoPath, String quickMarcJsonPath) throws JSONException {
    logger.info("Testing ParsedRecord -> QuickMarcJson conversion (expected flow + edge cases)");
    MarcBibliographicDtoConverter converter = new MarcBibliographicDtoConverter();
    ParsedRecordDto parsedRecordDto = getMockAsObject(parsedRecordDtoPath, ParsedRecordDto.class);
    QuickMarc quickMarcJson = converter.convert(parsedRecordDto);

    var expected = readQuickMarc(quickMarcJsonPath);
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(quickMarcJson), true);

  }
}
