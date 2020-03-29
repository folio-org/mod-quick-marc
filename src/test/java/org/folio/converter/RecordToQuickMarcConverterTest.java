package org.folio.converter;

import static org.folio.converter.TestUtils.getMockAsJson;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class RecordToQuickMarcConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(RecordToQuickMarcConverterTest.class);

  @Test
  public void testRecordToQuickMarcJsonConversion(){
    logger.info("Testing Record -> QuickMarcJson conversion");
    RecordToQuickMarcConverter converter = new RecordToQuickMarcConverter();
    Record record = getMockAsJson("mockdata/srs-records/record.json").mapTo(Record.class);
    QuickMarcJson quickMarcJson = converter.convert(record.getParsedRecord());
    QuickMarcJson expected = getMockAsJson("mockdata/quick-marc-json/quickMarcJson.json").mapTo(QuickMarcJson.class);
    String expectedString = JsonObject.mapFrom(expected).encodePrettily();
    String convertedString = JsonObject.mapFrom(quickMarcJson).encodePrettily();
    Assertions.assertEquals(expectedString, convertedString);
  }
}
