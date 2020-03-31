package org.folio.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.folio.converter.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ParsedRecordToQuickMarcConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(ParsedRecordToQuickMarcConverterTest.class);

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
