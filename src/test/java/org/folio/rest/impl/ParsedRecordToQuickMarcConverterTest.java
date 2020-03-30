package org.folio.rest.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.folio.rest.impl.utils.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.converter.ParsedRecordToQuickMarcConverter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecordDto;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ParsedRecordToQuickMarcConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(ParsedRecordToQuickMarcConverterTest.class);

  @Test
  public void testParsedRecordDtoToQuickMarcJsonConversion(){
    logger.info("Testing ParsedRecordDto -> QuickMarcJson conversion");
    ParsedRecordToQuickMarcConverter converter = new ParsedRecordToQuickMarcConverter();
    ParsedRecordDto parsedRecordDto = getMockAsJson("mockdata/parsedRecordDto.json").mapTo(ParsedRecordDto.class);
    QuickMarcJson quickMarcJson = converter.convert(parsedRecordDto.getParsedRecord());
    QuickMarcJson expected = getMockAsJson("mockdata/quickMarcJson.json").mapTo(QuickMarcJson.class);
    String expectedString = new String(JsonObject.mapFrom(expected).encodePrettily().getBytes(StandardCharsets.UTF_8));
    String convertedString = JsonObject.mapFrom(quickMarcJson).encodePrettily();
    assertEquals(expectedString, convertedString);
  }
}
