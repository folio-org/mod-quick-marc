package org.folio.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.folio.converter.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.Record;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class QuickMarcToParsedRecordConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToParsedRecordConverterTest.class);

  @Test
  public void testRecordsAreEqual(){
    logger.info("Source record and converted/restored one should be equal");
    QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter = new QuickMarcToParsedRecordConverter();
    ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter = new ParsedRecordToQuickMarcConverter();
    Record record = getMockAsJson("mockdata/srs-records/record.json").mapTo(Record.class);
    QuickMarcJson quickMarcJson = parsedRecordToQuickMarcConverter.convert(record.getParsedRecord());
    ParsedRecord restoredParsedRecord = quickMarcToParsedRecordConverter.convert(quickMarcJson);
    String sourceParsedRecord = new String(JsonObject.mapFrom(record.getParsedRecord()).encodePrettily().getBytes(
      StandardCharsets.UTF_8));
    String restoredRecord = JsonObject.mapFrom(restoredParsedRecord).encodePrettily();
    assertEquals(sourceParsedRecord, restoredRecord);
  }

  @Test
  public void exceptionWhen008WrongLength() {
    logger.info("Testing field 008 wrong length after editing - WrongField008LengthException expected");
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    QuickMarcJson wrongFile = getMockAsJson("mockdata/quick-marc-json/quickMarcWrong008Items.json").mapTo(QuickMarcJson.class);
    assertThrows(IllegalArgumentException.class, () -> converter.convert(wrongFile));
  }
}
