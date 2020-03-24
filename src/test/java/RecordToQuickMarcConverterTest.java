import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.converter.ContentType;
import org.folio.converter.Field008SplitterFactory;
import org.folio.converter.RecordToQuickMarcConverter;
import org.folio.exeptions.EmptyRawRecordException;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class RecordToQuickMarcConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(RecordToQuickMarcConverterTest.class);

  @Test
  public void exceptionIsThrownWhenNoRecord() {
    logger.info("Testing empty field 008  - EmptyRawRecordException expected");
    RecordToQuickMarcConverter converter = new RecordToQuickMarcConverter();
    Record emptyRecord = getMockAsJson("mockdata/recordWithEmptyRawRecord.json").mapTo(Record.class);
    assertThrows(EmptyRawRecordException.class, () -> converter.convert(emptyRecord));
  }

  @Test
  public void testRecordToQuickMarcJsonConversion(){
    logger.info("Testing Record -> QuickMarcJson conversion");
    RecordToQuickMarcConverter converter = new RecordToQuickMarcConverter();
    Record record = getMockAsJson("mockdata/record.json").mapTo(Record.class);
    QuickMarcJson quickMarcJson = converter.convert(record);
    QuickMarcJson expected = getMockAsJson("mockdata/quickMarcJson.json").mapTo(QuickMarcJson.class);
    String expectedString = JsonObject.mapFrom(expected).encodePrettily();
    String convertedString = JsonObject.mapFrom(quickMarcJson).encodePrettily();
    Assertions.assertEquals(expectedString, convertedString);
  }
}
