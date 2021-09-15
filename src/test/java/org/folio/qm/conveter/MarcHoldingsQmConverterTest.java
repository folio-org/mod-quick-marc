package org.folio.qm.conveter;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;

import static org.folio.qm.utils.AssertionUtils.mockIsEqualToObject;
import static org.folio.qm.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJsonNode;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO2_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_EDITED_RECORD_HOLDINGS_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.RESTORED_PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.TESTED_TAG_NAME;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getFieldWithIndicators;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getFieldWithValue;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getParsedRecordDtoWithMinContent;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.getQuickMarcJsonWithMinContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.folio.qm.converter.impl.MarcHoldingsDtoConverter;
import org.folio.qm.converter.impl.MarcHoldingsQmConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.utils.testentities.LccnFieldsTestEntities;
import org.folio.qm.utils.testentities.PhysicalDescriptionsTestEntities;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

class MarcHoldingsQmConverterTest {

  public static final String CONTENT = "content";
  public static final String FIELDS = "fields";
  public static final String IND_1 = "ind1";
  public static final String IND_2 = "ind2";
  private static final Logger logger = LogManager.getLogger(MarcHoldingsQmConverterTest.class);

  @ParameterizedTest
  @EnumSource(PhysicalDescriptionsTestEntities.class)
  void testRestorePhysicalCharacteristicsControlField(PhysicalDescriptionsTestEntities testEntity) {
    logger.info("Testing Characteristics field restoring for {}", testEntity.name());
    QuickMarc quickMarcJson = getMockAsObject(testEntity.getQuickMarcJsonPath(), QuickMarc.class);
    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    ParsedRecordDto parsedRecordDto = converter.convert(quickMarcJson);
    assertThat(parsedRecordDto, notNullValue());
    mockIsEqualToObject(testEntity.getParsedRecordPath(), parsedRecordDto.getParsedRecord());
  }

  @Test
  void testRestoreHoldingsGeneralCharacteristicsControlField() {
    logger.info("Testing Holdings General Information restoring");
    QuickMarc quickMarcJson = getMockAsObject(QM_RECORD_HOLDINGS, QuickMarc.class);
    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    ParsedRecordDto parsedRecordDto = converter.convert(quickMarcJson);
    assertThat(parsedRecordDto, notNullValue());
    mockIsEqualToObject(PARSED_RECORD_HOLDINGS_DTO2_PATH, parsedRecordDto);
  }

  @Test
  void testHoldingsGeneralCharacteristicsControlFieldUnknownFieldAdded() {
    logger.info("Testing Holdings General Information wrong element added - unknown property should be ignored");
    QuickMarc quickMarc = getMockAsObject(QM_RECORD_HOLDINGS, QuickMarc.class);
    var content = (LinkedHashMap<String, String>)quickMarc.getFields()
        .stream()
        .filter(fieldItem -> fieldItem.getTag().equals("008"))
        .collect(Collectors.toList())
        .get(0)
        .getContent();
    content.put("invalid_key", "invalid_value");
    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    ParsedRecordDto parsedRecordDto = converter.convert(quickMarc);
    assertThat(parsedRecordDto, notNullValue());
    mockIsEqualToObject(PARSED_RECORD_HOLDINGS_DTO2_PATH, parsedRecordDto);
  }

  @Test
  void testHoldingsGeneralCharacteristicsControlFieldWrongLength() {
    logger.info("Testing Holdings General Information wrong field length after editing - ConverterException expected");
    QuickMarc quickMarc = getMockAsObject(QM_RECORD_HOLDINGS, QuickMarc.class);
    var content = (LinkedHashMap<String, String>)quickMarc.getFields()
        .stream()
        .filter(fieldItem -> fieldItem.getTag().equals("008"))
        .collect(Collectors.toList())
        .get(0)
        .getContent();
    content.put("Copies", "1234");
    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    assertThrows(ConverterException.class, () -> converter.convert(quickMarc));
  }

  @Test
  void testQuickMarcJsonToParsedRecordDtoConversion() {
    logger.info("Testing QuickMarcJson -> ParsedRecordDto conversion");
    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    QuickMarc quickMarcJson = getMockAsObject(QM_EDITED_RECORD_HOLDINGS_PATH, QuickMarc.class);
    ParsedRecordDto parsedRecordDto = converter.convert(quickMarcJson);
    mockIsEqualToObject(RESTORED_PARSED_RECORD_HOLDINGS_DTO_PATH, parsedRecordDto);
  }

  @Test
  void testRecordsAreEqual() {
    logger.info("Source record and converted/restored one should be equal");
    MarcHoldingsQmConverter qmConverter = new MarcHoldingsQmConverter();
    MarcHoldingsDtoConverter dtoConverter = new MarcHoldingsDtoConverter();
    ParsedRecord parsedRecord = getMockAsObject(PARSED_RECORD_HOLDINGS_DTO_PATH, ParsedRecordDto.class).getParsedRecord();
    QuickMarc quickMarcJson = dtoConverter.convert(getParsedRecordDtoWithMinContent(parsedRecord,
      ParsedRecordDto.RecordType.MARC_HOLDING));
    assertThat(quickMarcJson, notNullValue());
    ParsedRecordDto restoredParsedRecordDto = qmConverter.convert(quickMarcJson);
    mockIsEqualToObject(RESTORED_PARSED_RECORD_HOLDINGS_DTO_PATH, restoredParsedRecordDto);
  }

  @Test
  void testEmptyIndicatorsList() {
    logger.info("Test empty indicators list");

    FieldItem testedField = getFieldWithIndicators(new ArrayList<>());
    FieldItem field001 = getFieldWithValue("001", "value");
    QuickMarc quickMarcJson = getQuickMarcJsonWithMinContent(field001, testedField);

    assertThat(quickMarcJson.getFields(), hasSize(2));
    assertThat(quickMarcJson.getFields().get(1).getIndicators(), hasSize(0));

    ObjectNode parsedRecordDto = getObjectAsJsonNode(new MarcHoldingsQmConverter().convert(quickMarcJson));
    JsonNode fieldJsonObject = parsedRecordDto.at("/parsedRecord/" + CONTENT + "/" + FIELDS + "/1/" + TESTED_TAG_NAME);

    assertThat(fieldJsonObject.get(IND_1).asText(), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.get(IND_2).asText(), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.size(), Is.is(3));
  }

  @Test
  void testListWithEmptyIndicators() {
    logger.info("Test list with empty indicators");

    FieldItem testedField = getFieldWithIndicators(Arrays.asList(" ", ""));
    FieldItem field001 = getFieldWithValue("001", "value");
    QuickMarc quickMarcJson = getQuickMarcJsonWithMinContent(field001, testedField);

    assertThat(quickMarcJson.getFields(), hasSize(2));
    assertThat(quickMarcJson.getFields().get(1).getIndicators(), hasSize(2));

    ObjectNode parsedRecordDto = getObjectAsJsonNode(new MarcHoldingsQmConverter().convert(quickMarcJson));
    JsonNode fieldJsonObject = parsedRecordDto.at("/parsedRecord/" + CONTENT + "/" + FIELDS + "/1/" + TESTED_TAG_NAME);

    assertThat(fieldJsonObject.get(IND_1).asText(), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.get(IND_2).asText(), equalTo(StringUtils.SPACE));
    assertThat(fieldJsonObject.size(), Is.is(3));
  }

  @Test
  void testIllegalNumberOfIndicators() {
    logger.info("Test illegal number of indicators - ConverterException expected");

    FieldItem testField = getFieldWithIndicators(Collections.singletonList("1"));
    FieldItem field001 = getFieldWithValue("001", "value");
    QuickMarc quickMarcJson = getQuickMarcJsonWithMinContent(field001, testField);

    assertThat(quickMarcJson.getFields(), hasSize(2));
    assertThat(quickMarcJson.getFields().get(1).getIndicators(), hasSize(1));

    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    assertThrows(ConverterException.class, () -> converter.convert(quickMarcJson));
  }

  @Test
  void testIllegalIndicatorSize() {
    logger.info("Test illegal number of indicators - ConverterException expected");

    FieldItem testField = getFieldWithIndicators(Arrays.asList(" ", " 1"));
    FieldItem field001 = getFieldWithValue("001", "value");
    QuickMarc quickMarcJson = getQuickMarcJsonWithMinContent(field001, testField);

    assertThat(quickMarcJson.getFields(), hasSize(2));
    assertThat(quickMarcJson.getFields().get(1).getIndicators(), hasSize(2));

    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    assertThrows(ConverterException.class, () -> converter.convert(quickMarcJson));
  }

  @ParameterizedTest
  @EnumSource(value = LccnFieldsTestEntities.class, mode = EXCLUDE, names = {"WRONG_LENGTH"})
  void testRestoreLccn(LccnFieldsTestEntities lccnField) {
    logger.info("Field 010 (LCCN) should match expected format");
    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    QuickMarc quickMarcJson = getMockAsObject(lccnField.getFilename(), QuickMarc.class);
    ParsedRecordDto parsedRecordDto = converter.convert(quickMarcJson);
    assertThat(parsedRecordDto, notNullValue());
    assertThat(getObjectAsJson(parsedRecordDto.getParsedRecord()),
      hasJsonPath("$.content.fields[1].['010'].subfields[0].a",
        matchesPattern("[a-z\\s]{2}\\d{10}|[a-z\\s]{3}\\d{8}\\s(/.*$)?")));
  }

  @ParameterizedTest
  @EnumSource(value = LccnFieldsTestEntities.class, mode = INCLUDE, names = {"WRONG_LENGTH"})
  void testLccnWrongLegth(LccnFieldsTestEntities lccnField) {
    logger.info("ConverterException should be thrown if field 010 has wrong length");
    MarcHoldingsQmConverter converter = new MarcHoldingsQmConverter();
    QuickMarc quickMarcJson = getMockAsObject(lccnField.getFilename(), QuickMarc.class);
    assertThrows(ConverterException.class, () -> converter.convert(quickMarcJson));
  }
}

