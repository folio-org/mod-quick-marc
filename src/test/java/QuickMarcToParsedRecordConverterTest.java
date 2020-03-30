import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.converter.ParsedRecordToQuickMarcConverter;
import org.folio.converter.QuickMarcToParsedRecordConverter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.ParsedRecordDto;
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
    ParsedRecordDto parsedRecordDto = getMockAsJson("mockdata/parsedRecordDto.json").mapTo(ParsedRecordDto.class);
    QuickMarcJson quickMarcJson = parsedRecordToQuickMarcConverter.convert(parsedRecordDto.getParsedRecord());
    ParsedRecord restoredParsedRecord = quickMarcToParsedRecordConverter.convert(quickMarcJson);
    String sourceParsedRecord = new String(JsonObject.mapFrom(parsedRecordDto.getParsedRecord()).encodePrettily().getBytes(
      StandardCharsets.UTF_8));
    String restoredRecord = JsonObject.mapFrom(restoredParsedRecord).encodePrettily();
    assertEquals(sourceParsedRecord, restoredRecord);
  }

  @Test
  public void exceptionWhen008WrongLength() {
    logger.info("Testing field 008 wrong length after editing - WrongField008LengthException expected");
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    QuickMarcJson wrongFile = getMockAsJson("mockdata/quickMarcWrong008Items.json").mapTo(QuickMarcJson.class);
    assertThrows(IllegalArgumentException.class, () -> converter.convert(wrongFile));
  }
}
