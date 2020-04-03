package org.folio.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.folio.converter.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.Record;
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
  public void testSplitFixedLengthControlField(TestEntities testEntity) {
    logger.info("Testing FixedLengthControlField splitting for {}", testEntity.getContentType().getName());
    ParsedRecord parsedRecord = getMockAsJson(testEntity.getParsedRecordPath()).mapTo(ParsedRecord.class);
    ParsedRecordToQuickMarcConverter converter = new ParsedRecordToQuickMarcConverter();
    QuickMarcJson quickMarcJson = converter.convert(parsedRecord);
    String actual = JsonObject.mapFrom(quickMarcJson).encodePrettily();
    String expected = getMockAsJson(testEntity.getQuickMarcJsonPath()).encodePrettily();
    assertEquals(expected, actual);
  }

  @Test
  public void testParsedRecordToQuickMarcJsonConversion(){
    logger.info("Testing ParsedRecord -> QuickMarcJson conversion");
    ParsedRecordToQuickMarcConverter converter = new ParsedRecordToQuickMarcConverter();
    Record record = getMockAsJson("mockdata/srs-records/record.json").mapTo(Record.class);
    QuickMarcJson quickMarcJson = converter.convert(record.getParsedRecord());
    QuickMarcJson expected = getMockAsJson("mockdata/quick-marc-json/quickMarcJson.json").mapTo(QuickMarcJson.class);
    String expectedString = new String(JsonObject.mapFrom(expected).encodePrettily().getBytes(StandardCharsets.UTF_8));
    String convertedString = JsonObject.mapFrom(quickMarcJson).encodePrettily();
    assertEquals(expectedString, convertedString);
  }
}
