package org.folio.converter;

import static org.folio.rest.impl.ApiTestBase.PARSED_RECORD_DTO_PATH;
import static org.folio.rest.impl.ApiTestBase.QUICK_MARC_RECORD_PATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.folio.converter.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.ParsedRecordDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ParsedRecordToQuickMarcConverterTest {

  private static final Logger logger = LoggerFactory.getLogger(ParsedRecordToQuickMarcConverterTest.class);

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  void testSplitFixedLengthControlField(TestEntities testEntity) {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.getContentType().getName());
    ParsedRecord parsedRecord = getMockAsJson(testEntity.getParsedRecordPath()).mapTo(ParsedRecord.class);
    ParsedRecordToQuickMarcConverter converter = new ParsedRecordToQuickMarcConverter();
    QuickMarcJson quickMarcJson = converter.convert(parsedRecord);
    assertThat(JsonObject.mapFrom(quickMarcJson), equalTo(getMockAsJson(testEntity.getQuickMarcJsonPath())));
  }

  @Test
  void testParsedRecordToQuickMarcJsonConversion(){
    logger.info("Testing ParsedRecord -> QuickMarcJson conversion");
    ParsedRecordToQuickMarcConverter converter = new ParsedRecordToQuickMarcConverter();
    ParsedRecordDto parsedRecordDto = getMockAsJson(PARSED_RECORD_DTO_PATH).mapTo(ParsedRecordDto.class);
    QuickMarcJson quickMarcJson = converter.convert(parsedRecordDto.getParsedRecord());
    QuickMarcJson expected = getMockAsJson(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class);
    String expectedString = new String(JsonObject.mapFrom(expected).encodePrettily().getBytes(StandardCharsets.UTF_8));
    String convertedString = JsonObject.mapFrom(quickMarcJson).encodePrettily();
    assertThat(expectedString, equalTo(convertedString));
  }
}
