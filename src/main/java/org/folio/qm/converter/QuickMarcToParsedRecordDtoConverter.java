package org.folio.qm.converter;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD;
import static org.folio.qm.converter.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.Constants.DESC;
import static org.folio.qm.converter.Constants.DESC_LEADER_POS;
import static org.folio.qm.converter.Constants.ELVL;
import static org.folio.qm.converter.Constants.ELVL_LEADER_POS;
import static org.folio.qm.converter.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.qm.converter.Constants.INSTANCE_HR_ID_CONTROL_FIELD;
import static org.folio.qm.converter.Constants.LCCN_TAG;
import static org.folio.qm.converter.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;
import static org.folio.qm.converter.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.converter.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.qm.converter.elements.ControlFieldItem.CATEGORY;
import static org.folio.qm.converter.elements.ControlFieldItem.VALUE;
import static org.folio.qm.util.ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FIELD;
import static org.folio.qm.util.ErrorCodes.ILLEGAL_INDICATORS_NUMBER;
import static org.folio.qm.util.ErrorCodes.ILLEGAL_SIZE_OF_INDICATOR;
import static org.folio.qm.util.ErrorCodes.LEADER_AND_008_MISMATCHING;
import static org.folio.qm.util.ErrorCodes.MISSED_001_FIELD;
import static org.folio.qm.util.ErrorUtils.buildInternalError;
import static org.folio.qm.util.MarcUtils.getFieldByTag;

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
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.AdditionalMaterialConfiguration;
import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.converter.elements.MaterialTypeConfiguration;
import org.folio.qm.converter.elements.PhysicalDescriptionFixedFieldElements;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.QuickMarcFields;
import org.folio.qm.exception.ConverterException;
import org.folio.rest.jaxrs.model.AdditionalInfo;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@Component
public class QuickMarcToParsedRecordDtoConverter implements Converter<QuickMarc, ParsedRecordDto> {

  private static final int GENERAL_INFORMATION_CONTROL_FIELD_LENGTH = 40;
  private static final int ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH = 18;
  private static final int LCCN_OLD_PREFIX_LENGTH = 3;
  private static final int LCCN_NEW_PREFIX_LENGTH = 2;
  private static final int TOKEN_MIN_LENGTH = 3;
  private static final String FIELDS = "fields";
  private static final String LEADER = "leader";
  private static final String INDICATOR1 = "ind1";
  private static final String INDICATOR2 = "ind2";
  private static final String SUBFIELDS = "subfields";
  private static final Pattern SPLIT_PATTERN = Pattern.compile("(?=[$][a-z0-9])");
  private static final String CONCAT_CONDITION_PATTERN = "(?:[$][1]\\s*|[$]\\d+(?:[.,])[^\\\\]*)$";
  private static final char SPACE_CHARACTER = ' ';
  private static final int ADDRESS_LENGTH = 12;
  private static final int TAG_LENGTH = 4;
  private static final int TERMINATOR_LENGTH = 1;
  private static final int LEADER_LENGTH = 24;
  private static final Pattern CONTROL_FIELD_PATTERN = Pattern.compile("^(00)[1-9]$");

  private static final String MISSED_001_MESSAGE = "001 field is required";

  private final MarcFactory factory = new MarcFactoryImpl();
  private String leaderString;
  private MaterialTypeConfiguration materialTypeConfiguration;

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarc quickMarc) {
    try {
      Record marcRecord = quickMarcJsonToMarcRecord(quickMarc);
      Map<String, Object> contentMap = new LinkedHashMap<>();
      contentMap.put(FIELDS, convertMarcFieldsToObjects(marcRecord));
      contentMap.put(LEADER, marcRecord.getLeader().marshal());
      return new ParsedRecordDto()
        .withParsedRecord(new ParsedRecord().withId(quickMarc.getParsedRecordId()).withContent(contentMap))
        .withRecordType(ParsedRecordDto.RecordType.MARC_BIB)
        .withId(quickMarc.getParsedRecordDtoId())
        .withExternalIdsHolder(constructExternalIdsHolder(quickMarc))
        .withAdditionalInfo(new AdditionalInfo().withSuppressDiscovery(quickMarc.getSuppressDiscovery()));
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private ExternalIdsHolder constructExternalIdsHolder(QuickMarc quickMarc) {
    var instanceId = quickMarc.getInstanceId();
    var instanceHrId = getFieldByTag(quickMarc, INSTANCE_HR_ID_CONTROL_FIELD)
        .map(quickMarcFields -> quickMarcFields.getContent().toString())
        .orElseThrow(() -> new ConverterException(buildInternalError(MISSED_001_FIELD, MISSED_001_MESSAGE)));
    return new ExternalIdsHolder().withInstanceId(instanceId).withInstanceHrid(instanceHrId);
  }

  private Record quickMarcJsonToMarcRecord(QuickMarc quickMarcJson) {
    Record marcRecord = factory.newRecord();

    leaderString = quickMarcJson.getLeader();
    materialTypeConfiguration = MaterialTypeConfiguration.resolveContentType(leaderString);

    quickMarcJson.getFields()
      .stream().map(this::toVariableField)
      .forEach(marcRecord::addVariableField);

    Leader leader = factory.newLeader(restoreBlanks(leaderString));
    leader.setRecordLength(calculateRecordLength(marcRecord));
    marcRecord.setLeader(leader);
    return marcRecord;
  }

  private VariableField toVariableField(QuickMarcFields field) {
    String tag = field.getTag();
    VariableField variableField;
    if (isControlField(field)) {
      variableField = factory.newControlField(tag, restoreControlFieldContent(tag, field.getContent()));
    } else {
      List<String> indicators = verifyAndGetIndicators(field);
      var dataField = factory.newDataField(tag, indicators.get(0).charAt(0), indicators.get(1).charAt(0));
      dataField.getSubfields().addAll(extractSubfields(field));
      variableField = dataField;
    }
    return variableField;
  }

  @SuppressWarnings("unchecked")
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
    var type = contentMap.get(ControlFieldItem.TYPE.getName()).toString();
    var additionalMaterialConfiguration = AdditionalMaterialConfiguration.resolveByCode(type.charAt(0));
    return restoreFixedLengthField(
      ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH, additionalMaterialConfiguration.getControlFieldItems(),
      contentMap, 0);
  }

  private String restorePhysicalDescriptionsControlField(Map<String, Object> contentMap) {
    char code = contentMap.get(CATEGORY.getName()).toString().charAt(0);
    PhysicalDescriptionFixedFieldElements physicalDescriptionFixedFieldElements =
      PhysicalDescriptionFixedFieldElements.resolveByCode(code);
    if (physicalDescriptionFixedFieldElements.equals(PhysicalDescriptionFixedFieldElements.UNKNOWN)) {
      return contentMap.get(VALUE.getName()).toString();
    } else {
      return restoreFixedLengthField(physicalDescriptionFixedFieldElements.getLength(),
        physicalDescriptionFixedFieldElements.getControlFieldItems(), contentMap, 0);
    }
  }

  private String restoreGeneralInformationControlField(Map<String, Object> contentMap) {
    if (isLeaderMatches(contentMap)) {
      String specificItemsString = restoreFixedLengthField(ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH - 1,
        materialTypeConfiguration.getControlFieldItems(), contentMap, -1);
      return new StringBuilder(
        restoreFixedLengthField(GENERAL_INFORMATION_CONTROL_FIELD_LENGTH, MaterialTypeConfiguration.getCommonItems(),
          contentMap, -1))
        .replace(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX, specificItemsString).toString();
    }
    throw new ConverterException(buildInternalError(LEADER_AND_008_MISMATCHING, "The Leader and 008 do not match"));
  }

  private boolean isLeaderMatches(Map<String, Object> contentMap) {
    return nonNull(contentMap) &&
      nonNull(contentMap.get(ELVL)) && nonNull(contentMap.get(DESC)) &&
      contentMap.get(ELVL).toString().equals(Character.toString(leaderString.charAt(ELVL_LEADER_POS))) &&
      contentMap.get(DESC).toString().equals(Character.toString(leaderString.charAt(DESC_LEADER_POS)));
  }

  @SuppressWarnings("unchecked")
  private String restoreFixedLengthField(int length, List<ControlFieldItem> items, Map<String, Object> map, int delta) {
    StringBuilder stringBuilder = new StringBuilder(StringUtils.repeat(SPACE_CHARACTER, length));
    items.forEach(item -> {
      String value;
      if (Objects.isNull(map.get(item.getName()))) {
        value = StringUtils.repeat(SPACE_CHARACTER, item.getLength());
      } else {
        value = item.isArray()
          ? String.join(EMPTY, ((List<String>) map.get(item.getName())))
          : map.get(item.getName()).toString();
        if (value.length() != item.getLength()) {
          throw new ConverterException(buildInternalError(ILLEGAL_FIXED_LENGTH_CONTROL_FIELD,
            String.format("Invalid %s field length, must be %d characters", item.getName(), item.getLength())));
        }
      }
      stringBuilder.replace(item.getPosition() + delta, item.getPosition() + delta + item.getLength(), value);
    });
    return stringBuilder.toString();
  }

  private List<Subfield> extractSubfields(QuickMarcFields field) {
    LinkedList<String> tokens = Arrays.stream(SPLIT_PATTERN.split(field.getContent().toString()))
      .collect(Collectors.toCollection(LinkedList::new));

    List<Subfield> subfields = new ArrayList<>();
    while (!tokens.isEmpty()) {
      String token = tokens.pop();
      String subfieldString = token.concat(checkNextToken(tokens));
      var subfield = LCCN_TAG.equals(field.getTag())
        ? lccnSubfieldFromString(subfieldString)
        : subfieldFromString(subfieldString);
      subfields.add(subfield);
    }

    return subfields;
  }

  private String checkNextToken(LinkedList<String> tokens) {
    return (!tokens.isEmpty() && tokens.peek().matches(CONCAT_CONDITION_PATTERN)) ?
      tokens.poll().concat(checkNextToken(tokens)) :
      EMPTY;
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
    throw new ConverterException(buildInternalError(ILLEGAL_FIXED_LENGTH_CONTROL_FIELD, "Illegal 010 (LCCN) subfield length"));
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
   * @param field {@link QuickMarc} field
   * @return true if field is Control Field, otherwise - false
   */
  private boolean isControlField(QuickMarcFields field) {
    return CONTROL_FIELD_PATTERN.matcher(field.getTag()).matches();
  }

  /**
   * This method returns indicators list of QuickMarc {@link QuickMarcFields}.
   *
   * @param field field from {@link QuickMarc}
   * @return list of indicators
   */
  private List<String> verifyAndGetIndicators(QuickMarcFields field) {
    List<String> indicators = field.getIndicators();
    if (indicators.size() == 2) {
      List<String> list = new ArrayList<>();
      list.add(retrieveIndicatorValue(field.getIndicators().get(0)));
      list.add(retrieveIndicatorValue(field.getIndicators().get(1)));
      return list;
    } else if (indicators.isEmpty()) {
      return Arrays.asList(SPACE, SPACE);
    } else {
      throw new ConverterException(buildInternalError(ILLEGAL_INDICATORS_NUMBER,
        String.format("Illegal indicators number for field: %s", field.getTag())));
    }
  }

  private String retrieveIndicatorValue(Object input) {
    if (Objects.isNull(input) || StringUtils.isEmpty(input.toString())) {
      return SPACE;
    } else {
      String indicator = restoreBlanks(input.toString());
      if (indicator.length() > 1) {
        throw new ConverterException(
          buildInternalError(ILLEGAL_SIZE_OF_INDICATOR, String.format("Illegal size of indicator: %s", indicator)));
      }
      return indicator;
    }
  }

  private String restoreBlanks(String sourceString) {
    return sourceString.replace(BLANK_REPLACEMENT, SPACE);
  }
}
