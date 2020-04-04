package org.folio.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

public class QuickMarcToParsedRecordConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToParsedRecordConverterTest.class);
  @ParameterizedTest
  @EnumSource(TestEntities.class)
  public void testRestoreFixedLengthControlField(TestEntities testEntity) {
    logger.info("Testing FixedLengthControlField restoring for {}", testEntity.getContentType().getName());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    ParsedRecord parsedRecord = converter.convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecord), equalTo(getMockAsJson(testEntity.getParsedRecordPath())));
  }

  @Test
  public void testRecordsAreEqual(){
    logger.info("Source record and converted/restored one should be equal");
    QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter = new QuickMarcToParsedRecordConverter();
    ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter = new ParsedRecordToQuickMarcConverter();
    Record record = getMockAsJson("mockdata/srs-records/record.json").mapTo(Record.class);
    QuickMarcJson quickMarcJson = parsedRecordToQuickMarcConverter.convert(record.getParsedRecord());
    ParsedRecord restoredParsedRecord = quickMarcToParsedRecordConverter.convert(quickMarcJson);
    String sourceParsedRecord = new String(JsonObject.mapFrom(record.getParsedRecord()).encodePrettily().getBytes(StandardCharsets.UTF_8));
    String restoredRecord = JsonObject.mapFrom(restoredParsedRecord).encodePrettily();
    assertThat(sourceParsedRecord, equalTo(restoredRecord));
  }

  @Test
  public void exceptionWhenFixedLengthControlFieldWrongLength() {
    logger.info("Testing FixedLengthControlField wrong length after editing - IllegalArgumentException expected");
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    QuickMarcJson wrongFile = getMockAsJson("mockdata/quick-marc-json/quickMarcWrongFixedLengthControlFieldItems.json").mapTo(QuickMarcJson.class);
    assertThrows(IllegalArgumentException.class, () -> converter.convert(wrongFile));
  }
}
