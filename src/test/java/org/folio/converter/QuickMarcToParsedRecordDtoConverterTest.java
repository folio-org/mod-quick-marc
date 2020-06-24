package org.folio.converter;

import static org.folio.converter.TestEntities.BOOKS;
import static org.folio.converter.TestUtils.getFieldWithIndicators;
import static org.folio.converter.TestUtils.getMockAsJson;
import static org.folio.converter.TestUtils.getParsedRecordDtoWithMinContent;
import static org.folio.converter.TestUtils.getQuickMarcJsonWithMinContent;
import static org.folio.rest.impl.ApiTestBase.PARSED_RECORD_DTO_PATH;
import static org.folio.rest.impl.ApiTestBase.QUICK_MARC_RECORD_PATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.folio.exception.ConverterException;
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

import io.vertx.core.json.JsonObject;

public class QuickMarcToParsedRecordDtoConverterTest {

  public static final String TESTED_TAG_NAME = "333";
  public static final String CONTENT = "content";
  public static final String FIELDS = "fields";
  public static final String IND_1 = "ind1";
  public static final String IND_2 = "ind2";
  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToParsedRecordDtoConverterTest.class);

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  void testRestoreGeneralAndAdditionalCharacteristicsControlFields(TestEntities testEntity) {
    logger.info("Testing general and additional characteristics restoring for {}", testEntity.getMaterialTypeConfiguration().getName());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    ParsedRecordDto parsedRecordDto = new QuickMarcToParsedRecordDtoConverter().convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecordDto.getParsedRecord()), equalTo(getMockAsJson(testEntity.getParsedRecordPath())));
  }

  @ParameterizedTest
  @EnumSource(PhysicalDescriptionsTestEntities.class)
  void testRestorePhysicalCharacteristicsControlField(PhysicalDescriptionsTestEntities testEntity) {
    logger.info("Testing Characteristics field restoring for {}", testEntity.name());
    QuickMarcJson quickMarcJson = getMockAsJson(testEntity.getQuickMarcJsonPath()).mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordDtoConverter converter = new QuickMarcToParsedRecordDtoConverter();
    ParsedRecordDto parsedRecordDto = converter.convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(parsedRecordDto.getParsedRecord()).encodePrettily(), equalTo(getMockAsJson(testEntity.getParsedRecordPath()).encodePrettily()));
  }

  @Test
  void testQuickMarcJsonToParsedRecordDtoConversion() {
    logger.info("Testing QuickMarcJson -> ParsedRecordDto conversion");
    QuickMarcToParsedRecordDtoConverter converter = new QuickMarcToParsedRecordDtoConverter();
    QuickMarcJson quickMarcJson = getMockAsJson(QUICK_MARC_RECORD_PATH).mapTo(QuickMarcJson.class);
    ParsedRecordDto parsedRecordDto = converter.convert(quickMarcJson);
    ParsedRecordDto expected = getMockAsJson(PARSED_RECORD_DTO_PATH).mapTo(ParsedRecordDto.class);
    assertThat(JsonObject.mapFrom(parsedRecordDto), equalTo(JsonObject.mapFrom(expected)));
  }

  @Test
  void testRecordsAreEqual() {
    logger.info("Source record and converted/restored one should be equal");
    QuickMarcToParsedRecordDtoConverter quickMarcToParsedRecordDtoConverter = new QuickMarcToParsedRecordDtoConverter();
    ParsedRecordDtoToQuickMarcConverter parsedRecordDtoToQuickMarcConverter = new ParsedRecordDtoToQuickMarcConverter();
    ParsedRecord parsedRecord = getMockAsJson(PARSED_RECORD_DTO_PATH).mapTo(ParsedRecordDto.class).getParsedRecord();
    QuickMarcJson quickMarcJson = parsedRecordDtoToQuickMarcConverter.convert(getParsedRecordDtoWithMinContent(parsedRecord));
    ParsedRecordDto restoredParsedRecordDto = quickMarcToParsedRecordDtoConverter.convert(quickMarcJson);
    assertThat(JsonObject.mapFrom(getParsedRecordDtoWithMinContent(parsedRecord)), equalTo(JsonObject.mapFrom(restoredParsedRecordDto)));
  }

  @Test
  void testFixedLengthControlFieldWrongLength() {
    logger.info("Testing FixedLengthControlField wrong length after editing - ConverterException expected");
    JsonObject json = getMockAsJson(BOOKS.getQuickMarcJsonPath());
    json.getJsonArray(FIELDS)
      .getJsonObject(3)
      .getJsonObject(CONTENT)
      .put("Entered", "abcdefg");
    QuickMarcJson quickMarcJson = json.mapTo(QuickMarcJson.class);
    QuickMarcToParsedRecordDtoConverter converter = new QuickMarcToParsedRecordDtoConverter();
    assertThrows(ConverterException.class, () -> converter.convert(quickMarcJson));
  }

  @Test
  void testEmptyIndicatorsList() {
    logger.info("Test empty indicators list");

    Field field = getFieldWithIndicators(new ArrayList<>());
    QuickMarcJson quickMarcJson = getQuickMarcJsonWithMinContent(field);

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(0));

    JsonObject parsedRecordDto = JsonObject.mapFrom(new QuickMarcToParsedRecordDtoConverter().convert(quickMarcJson));
    JsonObject fieldJsonObject = parsedRecordDto.getJsonObject("parsedRecord")
      .getJsonObject(CONTENT)
      .getJsonArray(FIELDS)
      .getJsonObject(0)
      .getJsonObject(TESTED_TAG_NAME);

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

    JsonObject parsedRecordDto = JsonObject.mapFrom(new QuickMarcToParsedRecordDtoConverter().convert(quickMarcJson));
    JsonObject fieldJsonObject = parsedRecordDto.getJsonObject("parsedRecord")
      .getJsonObject(CONTENT)
      .getJsonArray(FIELDS)
      .getJsonObject(0)
      .getJsonObject(TESTED_TAG_NAME);

    assertThat(fieldJsonObject.getString(IND_1), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.getString(IND_2), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.size(), Is.is(3));
  }

  @Test
  void testIllegalNumberOfIndicators() {
    logger.info("Test illegal number of indicators - ConverterException expected");

    Field field = getFieldWithIndicators(Collections.singletonList("1"));
    QuickMarcJson quickMarcJson = getQuickMarcJsonWithMinContent(field);

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(1));

    QuickMarcToParsedRecordDtoConverter converter = new QuickMarcToParsedRecordDtoConverter();
    assertThrows(ConverterException.class, () -> converter.convert(quickMarcJson));
  }

  @Test
  void testIllegalIndicatorSize() {
    logger.info("Test illegal number of indicators - ConverterException expected");

    Field field = getFieldWithIndicators(Arrays.asList(" ", " 1"));
    QuickMarcJson quickMarcJson = getQuickMarcJsonWithMinContent(field);

    assertThat(quickMarcJson.getFields(), hasSize(1));
    assertThat(quickMarcJson.getFields().get(0).getIndicators(), hasSize(2));

    QuickMarcToParsedRecordDtoConverter converter = new QuickMarcToParsedRecordDtoConverter();
    assertThrows(ConverterException.class, () -> converter.convert(quickMarcJson));
  }
}
