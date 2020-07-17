package org.folio.converter;

import static org.folio.converter.TestUtils.getMockAsJson;
import static org.folio.converter.TestUtils.getParsedRecordDtoWithMinContent;
import static org.folio.rest.impl.ApiTestBase.PARSED_RECORD_DTO_PATH;
import static org.folio.rest.impl.ApiTestBase.QUICK_MARC_RECORD_PATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.ParsedRecordDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class ParsedRecordDtoToQuickMarcConverterTest {

  private static final Logger logger = LoggerFactory.getLogger(ParsedRecordDtoToQuickMarcConverterTest.class);

  @ParameterizedTest
  @EnumSource(value = TestEntities.class, mode = EXCLUDE, names = { "BOOKS_MISSING_ITEMS" })
  void testSplitFixedLengthControlField(TestEntities testEntity)
  {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.getMaterialTypeConfiguration().getName());
    ParsedRecord parsedRecord = getMockAsJson(testEntity.getParsedRecordPath()).mapTo(ParsedRecord.class);
    ParsedRecordDtoToQuickMarcConverter converter = new ParsedRecordDtoToQuickMarcConverter();
    QuickMarcJson quickMarcJson = converter.convert(getParsedRecordDtoWithMinContent(parsedRecord));
    assertThat(JsonObject.mapFrom(quickMarcJson), equalTo(JsonObject.mapFrom(getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class))));
  }

  @ParameterizedTest
  @EnumSource(value = PhysicalDescriptionsTestEntities.class, mode = EXCLUDE, names = { "ELECTRONIC_RESOURCE_MISSING_ITEMS" })
  void testSplitFixedLengthControlField(PhysicalDescriptionsTestEntities testEntity) {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.name());
    ParsedRecord parsedRecord = getMockAsJson(testEntity.getParsedRecordPath()).mapTo(ParsedRecord.class);
    ParsedRecordDtoToQuickMarcConverter converter = new ParsedRecordDtoToQuickMarcConverter();
    QuickMarcJson quickMarcJson = converter.convert(getParsedRecordDtoWithMinContent(parsedRecord));
    assertThat(JsonObject.mapFrom(quickMarcJson), equalTo(JsonObject.mapFrom(getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class))));
  }

  @Test
  void testParsedRecordToQuickMarcJsonConversion() {
    logger.info("Testing ParsedRecord -> QuickMarcJson conversion");
    ParsedRecordDtoToQuickMarcConverter converter = new ParsedRecordDtoToQuickMarcConverter();
    ParsedRecordDto parsedRecordDto = getMockAsJson(PARSED_RECORD_DTO_PATH).mapTo(ParsedRecordDto.class);
    QuickMarcJson quickMarcJson = converter.convert(parsedRecordDto);
    QuickMarcJson expected = getMockAsJson(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class);
    assertThat(JsonObject.mapFrom(quickMarcJson), equalTo(JsonObject.mapFrom(expected)));
  }
}
