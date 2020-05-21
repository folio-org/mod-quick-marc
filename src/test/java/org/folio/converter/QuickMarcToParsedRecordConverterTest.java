package org.folio.converter;

import static org.folio.converter.TestEntities.BOOKS;
import static org.folio.rest.impl.ApiTestBase.PARSED_RECORD_DTO_PATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.folio.converter.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.ParsedRecordDto;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class QuickMarcToParsedRecordConverterTest {

  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToParsedRecordConverterTest.class);

  public static final String TESTED_TAG_NAME = "333";

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  void testRestoreFixedLengthControlField(TestEntities testEntity) {
    logger.info("Testing FixedLengthControlField restoring for {}", testEntity.getContentType().getName());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    ParsedRecord parsedRecord = new QuickMarcToParsedRecordConverter().convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecord), equalTo(getMockAsJson(testEntity.getParsedRecordPath())));
  }

  @Test
  void testRecordsAreEqual(){
    logger.info("Source record and converted/restored one should be equal");
    QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter = new QuickMarcToParsedRecordConverter();
    ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter = new ParsedRecordToQuickMarcConverter();
    ParsedRecordDto record = getMockAsJson(PARSED_RECORD_DTO_PATH).mapTo(ParsedRecordDto.class);
    QuickMarcJson quickMarcJson = parsedRecordToQuickMarcConverter.convert(record.getParsedRecord());
    ParsedRecord restoredParsedRecord = quickMarcToParsedRecordConverter.convert(quickMarcJson);
    String sourceParsedRecord = new String(JsonObject.mapFrom(record.getParsedRecord()).encodePrettily().getBytes(StandardCharsets.UTF_8));
    String restoredRecord = JsonObject.mapFrom(restoredParsedRecord).encodePrettily();
    assertThat(sourceParsedRecord, equalTo(restoredRecord));
  }

  @Test
  void testFixedLengthControlFieldWrongLength() {
    logger.info("Testing FixedLengthControlField wrong length after editing - IllegalArgumentException expected");
    JsonObject json = getMockAsJson(BOOKS.getQuickMarcJsonPath());
    json.getJsonArray("fields").getJsonObject(2).getJsonObject("content").put("Entered", "abcdefg");
    QuickMarcJson quickMarcJson = json.mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    assertThrows(IllegalArgumentException.class, () -> converter.convert(quickMarcJson));
  }

  @Test
  void testEmptyIndicatorsList() {
    logger.info("Test empty indicators list");

    Field field = new Field()
      .withTag(TESTED_TAG_NAME)
      .withContent("$333 content")
      .withIndicators(new ArrayList<>());

    QuickMarcJson quickMarcJson = new QuickMarcJson()
      .withLeader("01542ccm a2200361   4500")
      .withFields(Collections.singletonList(field));

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(0));

    JsonObject parsedRecord = JsonObject.mapFrom(new QuickMarcToParsedRecordConverter().convert(quickMarcJson));
    JsonObject fieldJsonObject = parsedRecord.getJsonObject("content").getJsonArray("fields").getJsonObject(0).getJsonObject(TESTED_TAG_NAME);

    String ind1 = fieldJsonObject.getString("ind1");
    String ind2 = fieldJsonObject.getString("ind2");

    assertThat(ind1, equalTo(StringUtils.SPACE));
    assertThat(ind2, equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.size(), Is.is(3));
  }

  @Test
  void testIllegalNumberOfIndicators() {
    logger.info("Test illegal number of indicators - IllegalArgumentException expected");

    Field field = new Field()
      .withTag(TESTED_TAG_NAME)
      .withContent("$333 content")
      .withIndicators(Collections.singletonList("1"));

    QuickMarcJson quickMarcJson = new QuickMarcJson()
      .withLeader("01542ccm a2200361   4500")
      .withFields(Collections.singletonList(field));

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(1));

    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    assertThrows(IllegalArgumentException.class, () -> converter.convert(quickMarcJson));
  }
}
