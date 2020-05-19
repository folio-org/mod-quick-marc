package org.folio.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.folio.converter.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
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
  public void testRestoreGeneralAndAdditionalCharacteristicsControlFields(TestEntities testEntity) {
    logger.info("Testing general and additional characteristics restoring for {}", testEntity.getContentType().getName());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    ParsedRecord parsedRecord = converter.convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecord), equalTo(getMockAsJson(testEntity.getParsedRecordPath())));
  }

  @ParameterizedTest
  @EnumSource(PhysicalDescriptionsTestEntities.class)
  public void testRestorePhysicalCharacteristicsControlField(PhysicalDescriptionsTestEntities testEntity) {
    logger.info("Testing Characteristics field restoring for {}", testEntity.name());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    ParsedRecord parsedRecord = converter.convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecord).encodePrettily(), equalTo(getMockAsJson(testEntity.getParsedRecordPath()).encodePrettily()));
  }

  @Test
  public void testRecordsAreEqual(){
    logger.info("Source record and converted/restored one should be equal");
    QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter = new QuickMarcToParsedRecordConverter();
    ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter = new ParsedRecordToQuickMarcConverter();
    ParsedRecord sourceParsedRecord = getMockAsJson("mockdata/parsed-records/parsedRecord.json").mapTo(ParsedRecord.class);
    QuickMarcJson quickMarcJson = parsedRecordToQuickMarcConverter.convert(sourceParsedRecord);
    ParsedRecord restoredParsedRecord = quickMarcToParsedRecordConverter.convert(quickMarcJson);
    String sourceParsedRecordString = new String(JsonObject.mapFrom(sourceParsedRecord).encodePrettily().getBytes(StandardCharsets.UTF_8));
    String restoredParsedRecordString = JsonObject.mapFrom(restoredParsedRecord).encodePrettily();
    assertThat(sourceParsedRecordString, equalTo(restoredParsedRecordString));
  }

  @Test
  public void exceptionWhenFixedLengthControlFieldWrongLength() {
    logger.info("Testing fixed length field with wrong values - IllegalArgumentException expected");
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    QuickMarcJson wrongFile = getMockAsJson("mockdata/quick-marc-json/quickMarcJsonWrongLength.json").mapTo(QuickMarcJson.class);
    assertThrows(IllegalArgumentException.class, () -> converter.convert(wrongFile));
  }
}
