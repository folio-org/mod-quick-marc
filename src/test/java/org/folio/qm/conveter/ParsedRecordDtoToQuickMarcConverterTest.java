package org.folio.qm.conveter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import static org.folio.qm.utils.TestUtils.PARSED_RECORD_DTO_PATH;
import static org.folio.qm.utils.TestUtils.PARSED_RECORD_EDGE_CASES_PATH;
import static org.folio.qm.utils.TestUtils.QM_RECORD_EDGE_CASES_PATH;
import static org.folio.qm.utils.TestUtils.QM_RECORD_PATH;
import static org.folio.qm.utils.TestUtils.getMockAsJson;
import static org.folio.qm.utils.TestUtils.getParsedRecordDtoWithMinContent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.folio.qm.utils.PhysicalDescriptionsTestEntities;
import org.folio.qm.utils.GeneralTestEntities;
import org.folio.qm.converter.ParsedRecordDtoToQuickMarcConverter;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;


import io.vertx.core.json.JsonObject;

public class ParsedRecordDtoToQuickMarcConverterTest {

  private static final Logger logger = LogManager.getLogger(ParsedRecordDtoToQuickMarcConverterTest.class);

  @ParameterizedTest
  @EnumSource(value = GeneralTestEntities.class, mode = EXCLUDE, names = { "BOOKS_MISSING_ITEMS" })
  void testSplitFixedLengthControlField(GeneralTestEntities testEntity) {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.getMaterialTypeConfiguration().getName());
    ParsedRecord parsedRecord = getMockAsJson(testEntity.getParsedRecordPath()).mapTo(ParsedRecord.class);
    ParsedRecordDtoToQuickMarcConverter converter = new ParsedRecordDtoToQuickMarcConverter();
    QuickMarc quickMarcJson = converter.convert(getParsedRecordDtoWithMinContent(parsedRecord));
    assertThat(JsonObject.mapFrom(quickMarcJson), equalTo(JsonObject.mapFrom(getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarc.class))));
  }

  @ParameterizedTest
  @EnumSource(value = PhysicalDescriptionsTestEntities.class, mode = EXCLUDE, names = { "ELECTRONIC_RESOURCE_MISSING_ITEMS" })
  void testSplitFixedLengthControlField(PhysicalDescriptionsTestEntities testEntity) {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.name());
    ParsedRecord parsedRecord = getMockAsJson(testEntity.getParsedRecordPath()).mapTo(ParsedRecord.class);
    ParsedRecordDtoToQuickMarcConverter converter = new ParsedRecordDtoToQuickMarcConverter();
    QuickMarc quickMarcJson = converter.convert(getParsedRecordDtoWithMinContent(parsedRecord));
    assertThat(JsonObject.mapFrom(quickMarcJson), equalTo(JsonObject.mapFrom(getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarc.class))));
  }

  @ParameterizedTest
  @CsvSource(value = {PARSED_RECORD_DTO_PATH + "," + QM_RECORD_PATH, PARSED_RECORD_EDGE_CASES_PATH + "," + QM_RECORD_EDGE_CASES_PATH})
  void testParsedRecordToQuickMarcJsonConversion(String parsedRecordDtoPath, String quickMarcJsonPath) {
    logger.info("Testing ParsedRecord -> QuickMarcJson conversion (expected flow + edge cases)");
    ParsedRecordDtoToQuickMarcConverter converter = new ParsedRecordDtoToQuickMarcConverter();
    ParsedRecordDto parsedRecordDto = getMockAsJson(parsedRecordDtoPath).mapTo(ParsedRecordDto.class);
    QuickMarc quickMarcJson = converter.convert(parsedRecordDto);
    QuickMarc expected = getMockAsJson(quickMarcJsonPath).mapTo(QuickMarc.class);
    assertThat(JsonObject.mapFrom(quickMarcJson), equalTo(JsonObject.mapFrom(expected)));
  }
}
