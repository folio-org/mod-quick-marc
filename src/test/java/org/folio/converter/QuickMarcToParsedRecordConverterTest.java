package org.folio.converter;

import static org.folio.converter.TestEntities.BOOKS;
import static org.folio.converter.TestUtils.getFieldWithIndicators;
import static org.folio.converter.TestUtils.getQuickMarcJsonWithMinContent;
import static org.folio.rest.impl.ApiTestBase.PARSED_RECORD_DTO_PATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.folio.converter.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.folio.exception.MarcConversionException;
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
import java.util.Arrays;
import java.util.Collections;

public class QuickMarcToParsedRecordConverterTest {

  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToParsedRecordConverterTest.class);

  public static final String TESTED_TAG_NAME = "333";
  public static final String CONTENT = "content";
  public static final String FIELDS = "fields";
  public static final String IND_1 = "ind1";
  public static final String IND_2 = "ind2";

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  void testRestoreGeneralAndAdditionalCharacteristicsControlFields(TestEntities testEntity) {
    logger.info("Testing general and additional characteristics restoring for {}", testEntity.getContentType().getName());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    ParsedRecord parsedRecord = new QuickMarcToParsedRecordConverter().convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecord), equalTo(getMockAsJson(testEntity.getParsedRecordPath())));
  }

  @ParameterizedTest
  @EnumSource(PhysicalDescriptionsTestEntities.class)
  void testRestorePhysicalCharacteristicsControlField(PhysicalDescriptionsTestEntities testEntity) {
    logger.info("Testing Characteristics field restoring for {}", testEntity.name());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    ParsedRecord parsedRecord = converter.convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecord).encodePrettily(), equalTo(getMockAsJson(testEntity.getParsedRecordPath()).encodePrettily()));
  }

  @Test
  void testRecordsAreEqual(){
    logger.info("Source record and converted/restored one should be equal");
    QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter = new QuickMarcToParsedRecordConverter();
    ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter = new ParsedRecordToQuickMarcConverter();
    ParsedRecord parsedRecord = getMockAsJson(PARSED_RECORD_DTO_PATH).mapTo(ParsedRecordDto.class).getParsedRecord();
    QuickMarcJson quickMarcJson = parsedRecordToQuickMarcConverter.convert(parsedRecord);
    ParsedRecord restoredParsedRecord = quickMarcToParsedRecordConverter.convert(quickMarcJson);
    String sourceParsedRecordString = new String(JsonObject.mapFrom(parsedRecord).encodePrettily().getBytes(StandardCharsets.UTF_8));
    String restoredParsedRecordString = JsonObject.mapFrom(restoredParsedRecord).encodePrettily();
    assertThat(sourceParsedRecordString, equalTo(restoredParsedRecordString));
  }

  @Test
  void testFixedLengthControlFieldWrongLength() {
    logger.info("Testing FixedLengthControlField wrong length after editing - MarcConversionException expected");
    JsonObject json = getMockAsJson(BOOKS.getQuickMarcJsonPath());
    json.getJsonArray(FIELDS).getJsonObject(3).getJsonObject(CONTENT).put("Entered", "abcdefg");
    QuickMarcJson quickMarcJson = json.mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    assertThrows(MarcConversionException.class, () -> converter.convert(quickMarcJson));
  }

  @Test
  void testEmptyIndicatorsList() {
    logger.info("Test empty indicators list");

    Field field = getFieldWithIndicators(new ArrayList<>());
    QuickMarcJson quickMarcJson = getQuickMarcJsonWithMinContent(field);

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(0));

    JsonObject parsedRecord = JsonObject.mapFrom(new QuickMarcToParsedRecordConverter().convert(quickMarcJson));
    JsonObject fieldJsonObject = parsedRecord.getJsonObject(CONTENT).getJsonArray(FIELDS).getJsonObject(0).getJsonObject(TESTED_TAG_NAME);

    assertThat(fieldJsonObject.getString(IND_1), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.getString(IND_2), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.size(), Is.is(3));
  }

  @Test
  void testListWithEmptyIndicators() {
    logger.info("Test list with empty indicators");

    Field field = getFieldWithIndicators(Arrays.asList(" ", ""));
    QuickMarcJson quickMarcJson = getQuickMarcJsonWithMinContent(field);

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(2));

    JsonObject parsedRecord = JsonObject.mapFrom(new QuickMarcToParsedRecordConverter().convert(quickMarcJson));
    JsonObject fieldJsonObject = parsedRecord.getJsonObject(CONTENT).getJsonArray(FIELDS).getJsonObject(0).getJsonObject(TESTED_TAG_NAME);

    assertThat(fieldJsonObject.getString(IND_1), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.getString(IND_2), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.size(), Is.is(3));
  }

  @Test
  void testIllegalNumberOfIndicators() {
    logger.info("Test illegal number of indicators - IllegalArgumentException expected");

    Field field = getFieldWithIndicators(Collections.singletonList("1"));
    QuickMarcJson quickMarcJson = getQuickMarcJsonWithMinContent(field);

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(1));

    QuickMarcToParsedRecordConverter converter = new QuickMarcToParsedRecordConverter();
    assertThrows(IllegalArgumentException.class, () -> converter.convert(quickMarcJson));
  }
}
