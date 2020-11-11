package org.folio.converter;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.folio.converter.Constants.BLANK_REPLACEMENT;
import static org.folio.converter.Constants.LCCN_TAG;
import static org.folio.converter.Constants.DESC_LEADER_POS;
import static org.folio.converter.Constants.ELVL_LEADER_POS;
import static org.folio.converter.Constants.TYPE_OF_RECORD_LEADER_POS;
import static org.folio.converter.elements.FixedLengthDataElements.CATEGORY;
import static org.folio.converter.elements.FixedLengthDataElements.VALUE;
import static org.folio.converter.elements.MaterialTypeConfiguration.UNKNOWN;
import static org.folio.converter.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD;
import static org.folio.converter.Constants.DESC;
import static org.folio.converter.Constants.ELVL;
import static org.folio.converter.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.converter.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;
import static org.folio.converter.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.converter.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.util.ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FILED;
import static org.folio.util.ErrorCodes.ILLEGAL_INDICATORS_NUMBER;
import static org.folio.util.ErrorCodes.ILLEGAL_SIZE_OF_INDICATOR;
import static org.folio.util.ErrorCodes.LEADER_AND_008_MISMATCHING;
import static org.folio.util.ErrorUtils.buildError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.folio.converter.elements.FixedLengthDataElements;
import org.folio.converter.elements.MaterialTypeConfiguration;
import org.folio.converter.elements.PhysicalDescriptionFixedFieldElements;
import org.folio.exception.ConverterException;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.AdditionalInfo;
import org.folio.srs.model.ExternalIdsHolder;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.ParsedRecordDto;
import org.folio.util.ErrorUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class QuickMarcToParsedRecordDtoConverter implements Converter<QuickMarcJson, ParsedRecordDto> {

  private static final int GENERAL_INFORMATION_CONTROL_FIELD_LENGTH = 40;
  private static final int ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH = 17;
  private static final int LCCN_OLD_PREFIX_LENGTH = 3;
  private static final int LCCN_NEW_PREFIX_LENGTH = 2;
  private static final int TOKEN_MIN_LENGTH = 3;
  private static final String FIELDS = "fields";
  private static final String LEADER = "leader";
  private static final String INDICATOR1 = "ind1";
  private static final String INDICATOR2 = "ind2";
  private static final String SUBFIELDS = "subfields";
  private static final String SPLIT_PATTERN = "(?=[$][a-z0-9])";
  private static final String CONCAT_CONDITION_PATTERN = "[$][1]\\s*?|[$]\\d+(?:[.,])[^\\\\]*$";
  private static final char SPACE_CHARACTER = ' ';
  private static final int ADDRESS_LENGTH = 12;
  private static final int TAG_LENGTH = 4;
  private static final int TERMINATOR_LENGTH = 1;
  private static final int LEADER_LENGTH = 24;
  private static final Pattern CONTROL_FIELD_PATTERN = Pattern.compile("^(00)[1-9]$");

  private final MarcFactory factory = new MarcFactoryImpl();
  private String leaderString;
  private MaterialTypeConfiguration materialTypeConfiguration;

  @Override
  public ParsedRecordDto convert(QuickMarcJson quickMarcJson) {
    try {
      Record marcRecord = quickMarcJsonToMarcRecord(quickMarcJson);

      Map<String, Object> contentMap = new LinkedHashMap<>();
      contentMap.put(FIELDS, convertMarcFieldsToObjects(marcRecord));
      contentMap.put(LEADER, marcRecord.getLeader()
        .marshal());

      return new ParsedRecordDto().withParsedRecord(new ParsedRecord().withId(quickMarcJson.getParsedRecordId())
        .withContent(contentMap))
        .withRecordType(ParsedRecordDto.RecordType.MARC)
        .withId(quickMarcJson.getParsedRecordDtoId())
        .withExternalIdsHolder(new ExternalIdsHolder().withInstanceId(quickMarcJson.getInstanceId()))
        .withAdditionalInfo(new AdditionalInfo().withSuppressDiscovery(quickMarcJson.getSuppressDiscovery()));

    } catch (Exception e) {
      throw new ConverterException(e, this.getClass());
    }
  }

  private Record quickMarcJsonToMarcRecord(QuickMarcJson quickMarcJson) {
    Record marcRecord = factory.newRecord();

    leaderString = quickMarcJson.getLeader();
    materialTypeConfiguration = MaterialTypeConfiguration.resolveContentType(leaderString);

    quickMarcJson.getFields()
      .forEach(field -> {
        String tag = field.getTag();
        if (isControlField(field)) {
          ControlField controlField = factory.newControlField();
          controlField.setTag(tag);
          controlField.setData(restoreControlFieldContent(tag, field.getContent()));
          marcRecord.getControlFields().add(controlField);
        } else {
          DataField dataField = factory.newDataField();
          dataField.setTag(field.getTag());
          dataField.getSubfields().addAll(convertFieldToSubfields(field));
          List<String> indicators = verifyAndGetIndicators(field);
          dataField.setIndicator1(indicators.get(0).toCharArray()[0]);
          dataField.setIndicator2(indicators.get(1).toCharArray()[0]);
          marcRecord.getDataFields().add(dataField);
        }
      });

    Leader leader = factory.newLeader(restoreBlanks(leaderString));
    leader.setRecordLength(calculateRecordLength(marcRecord));
    marcRecord.setLeader(leader);
    return marcRecord;
  }

  private String restoreControlFieldContent(String tag, Object content) {
    switch (tag) {
    case ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD:
      return restoreBlanks(restoreAdditionalCharacteristicsControlField((Map<String, Object>) content));
    case PHYSICAL_DESCRIPTIONS_CONTROL_FIELD:
      return restoreBlanks(restorePhysicalDescriptionsControlField((Map<String, Object>) content));
    case GENERAL_INFORMATION_CONTROL_FIELD:
      return restoreBlanks(restoreGeneralInformationControlField((Map<String, Object>) content));
    default:
      return content.toString();
    }
  }

  private String restoreAdditionalCharacteristicsControlField(Map<String, Object> contentMap) {
    if (materialTypeConfiguration.equals(UNKNOWN)) {
      return contentMap.get(VALUE.getName()).toString();
    } else {
      return leaderString.charAt(TYPE_OF_RECORD_LEADER_POS) + restoreFixedLengthField(ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH, materialTypeConfiguration.getFixedLengthControlFieldItems(), contentMap);
    }
  }

  private String restorePhysicalDescriptionsControlField(Map<String, Object> contentMap) {
    char code = contentMap.get(CATEGORY.getName()).toString().charAt(0);
    PhysicalDescriptionFixedFieldElements physicalDescriptionFixedFieldElements = PhysicalDescriptionFixedFieldElements.resolveByCode(code);
    if (physicalDescriptionFixedFieldElements.equals(PhysicalDescriptionFixedFieldElements.UNKNOWN)) {
      return contentMap.get(VALUE.getName()).toString();
    } else {
      return restoreFixedLengthField(physicalDescriptionFixedFieldElements.getLength(), physicalDescriptionFixedFieldElements.getItems(), contentMap);
    }
  }

  private String restoreGeneralInformationControlField(Map<String, Object> contentMap) {
    if (!contentMap.get(ELVL).toString().equals(Character.toString(leaderString.charAt(ELVL_LEADER_POS))) ||
      !contentMap.get(DESC).toString().equals(Character.toString(leaderString.charAt(DESC_LEADER_POS)))) {
      throw new ConverterException(buildError(LEADER_AND_008_MISMATCHING, ErrorUtils.ErrorType.INTERNAL,"The Leader and 008 do not match"));
    }
    String specificItemsString = restoreFixedLengthField(ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH, materialTypeConfiguration.getFixedLengthControlFieldItems(), contentMap);
    return new StringBuilder(restoreFixedLengthField(GENERAL_INFORMATION_CONTROL_FIELD_LENGTH, MaterialTypeConfiguration.getCommonItems(), contentMap))
      .replace(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX, specificItemsString).toString();
  }

  private String restoreFixedLengthField(int length, List<FixedLengthDataElements> items, Map<String, Object> map) {
    StringBuilder stringBuilder = new StringBuilder(StringUtils.repeat(SPACE_CHARACTER, length));
    items.forEach(item -> {
      String value;
      if (Objects.isNull(map.get(item.getName()))) {
        value = StringUtils.repeat(SPACE_CHARACTER, item.getLength());
      } else {
        value = item.isArray() ? String.join(EMPTY, ((List<String>) map.get(item.getName()))) : map.get(item.getName()).toString();
        if (value.length() != item.getLength()) {
          throw new ConverterException(buildError(ILLEGAL_FIXED_LENGTH_CONTROL_FILED, ErrorUtils.ErrorType.INTERNAL, String.format("Invalid %s field length, must be %d characters", item.getName(), item.getLength())));
        }
      }
      stringBuilder.replace(item.getPosition(), item.getPosition() + item.getLength(), value);
    });
    return stringBuilder.toString();
  }

  private List<Subfield> convertFieldToSubfields(Field field) {
    LinkedList<String> tokens = Arrays.stream(field.getContent().toString().split(SPLIT_PATTERN))
      .collect(Collectors.toCollection(LinkedList::new));

    List<Subfield> subfields = new ArrayList<>();
    while (!tokens.isEmpty()) {
      String subfieldString = tokens.pop();
      subfieldString = subfieldString.concat(checkNextToken(tokens));
      subfields.add(LCCN_TAG.equals(field.getTag()) ? lccnSubfieldFromString(subfieldString) : subfieldFromString(subfieldString));
    }

    return subfields;
  }

  private String checkNextToken(LinkedList<String> tokens) {
    return (!tokens.isEmpty() && tokens.peek().matches(CONCAT_CONDITION_PATTERN)) ? tokens.poll().concat(checkNextToken(tokens)) : EMPTY;
  }

  private Subfield subfieldFromString(String string) {
    return new SubfieldImpl(string.charAt(1), string.length() < TOKEN_MIN_LENGTH ? EMPTY : string.substring(2).trim());
  }

  private Subfield lccnSubfieldFromString(String string) {
    if (string.length() >= TOKEN_MIN_LENGTH) {
      String lccnString = string.substring(2).trim();
      if (string.matches("[$][abz].*$")) {
        if (lccnString.matches("\\d{10}")) {
          lccnString = StringUtils.repeat(SPACE_CHARACTER, LCCN_NEW_PREFIX_LENGTH).concat(lccnString);
        } else if (lccnString.matches("\\d{8}")) {
          lccnString = StringUtils.repeat(SPACE_CHARACTER, LCCN_OLD_PREFIX_LENGTH).concat(lccnString).concat(SPACE);
        } else if (lccnString.matches("\\d{8}\\s/.*$")) {
          lccnString = StringUtils.repeat(SPACE_CHARACTER, LCCN_OLD_PREFIX_LENGTH).concat(lccnString);
        } else if (lccnString.matches("[a-z\\s]{3}\\d{8}")) {
          lccnString = lccnString.concat(SPACE);
        }
      }
      return new SubfieldImpl(string.charAt(1), lccnString);
    }
    throw new ConverterException(buildError(ILLEGAL_FIXED_LENGTH_CONTROL_FILED, ErrorUtils.ErrorType.INTERNAL, "Illegal 010 (LCCN) subfield length"));
  }

  private List<Object> convertMarcFieldsToObjects(Record marcRecord) {

    List<Object> fields = marcRecord.getControlFields()
      .stream()
      .map(cf -> Collections.singletonMap(cf.getTag(), cf.getData()))
      .collect(Collectors.toList());

    fields.addAll(marcRecord.getDataFields()
      .stream()
      .map(df -> Collections.singletonMap(df.getTag(), convertDataFieldToField(df)))
      .collect(Collectors.toList()));

    return fields;
  }

  private Map<String, Object> convertDataFieldToField(DataField dataField) {
    Map<String, Object> fieldMap = new LinkedHashMap<>();
    fieldMap.put(INDICATOR1, Character.toString(dataField.getIndicator1()));
    fieldMap.put(INDICATOR2, Character.toString(dataField.getIndicator2()));
    fieldMap.put(SUBFIELDS, dataField.getSubfields()
      .stream()
      .map(sf -> Collections.singletonMap(Character.toString(sf.getCode()), sf.getData()))
      .collect(Collectors.toList()));
    return fieldMap;
  }

  private int calculateRecordLength(Record record) {
    int addressesLength = record.getVariableFields().size() * ADDRESS_LENGTH;
    int controlFieldsLength = record.getControlFields()
      .stream()
      .mapToInt(controlField -> controlField.getData().length() + TERMINATOR_LENGTH)
      .sum();
    int dataFieldsLength = record.getDataFields()
      .stream()
      .mapToInt(dataField -> dataField.toString().length() - TAG_LENGTH + TERMINATOR_LENGTH)
      .sum();
    return LEADER_LENGTH + addressesLength + controlFieldsLength + dataFieldsLength + TERMINATOR_LENGTH;
  }

  /**
   * This method determines if field is Control Filed of MARC record based on pattern 00X according to MARC record Format
   * specification.
   *
   * @param field {@link QuickMarcJson} field
   * @return true if field is Control Field, otherwise - false
   */
  private boolean isControlField(Field field) {
    return CONTROL_FIELD_PATTERN.matcher(field.getTag()).matches();
  }

  /**
   * This method returns indicators list of QuickMarcJson {@link Field}.
   *
   * @param field field from {@link QuickMarcJson}
   * @return list of indicators
   */
  private List<String> verifyAndGetIndicators(Field field) {
    List<Object> indicators = field.getIndicators();
    if (indicators.size() == 2) {
      List<String> list = new ArrayList<>();
      list.add(retrieveIndicatorValue(field.getIndicators().get(0)));
      list.add(retrieveIndicatorValue(field.getIndicators().get(1)));
      return list;
    } else if (indicators.isEmpty()) {
      return Arrays.asList(SPACE, SPACE);
    } else {
      throw new ConverterException(buildError(ILLEGAL_INDICATORS_NUMBER, ErrorUtils.ErrorType.INTERNAL,"Illegal indicators number for field: " + field.getTag()));
    }
  }

  private String retrieveIndicatorValue(Object input) {
    if (Objects.isNull(input) || StringUtils.isEmpty(input.toString())) {
      return SPACE;
    } else {
      String indicator = restoreBlanks(input.toString());
      if (indicator.length() > 1) {
        throw new ConverterException(buildError(ILLEGAL_SIZE_OF_INDICATOR, ErrorUtils.ErrorType.INTERNAL,"Illegal size of indicator: " + indicator));
      }
      return indicator;
    }
  }

  private String restoreBlanks(String sourceString) {
    return sourceString.replace(BLANK_REPLACEMENT, SPACE);
  }
}
