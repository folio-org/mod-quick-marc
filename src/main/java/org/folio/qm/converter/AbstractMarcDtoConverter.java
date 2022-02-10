package org.folio.qm.converter;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.folio.qm.converter.elements.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.elements.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSetting;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.springframework.lang.NonNull;

import org.folio.qm.converter.elements.AdditionalMaterialConfiguration;
import org.folio.qm.converter.elements.Constants;
import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.converter.elements.PhysicalDescriptionFixedFieldElements;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.UpdateInfo;
import org.folio.qm.exception.ConverterException;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@AllArgsConstructor
public abstract class AbstractMarcDtoConverter implements MarcDtoConverter {
  private static final String ANY_STRING = "*";
  private static final char BLANK_SUBFIELD_CODE = ' ';

  private MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc;

  @Override
  public QuickMarc convert(@NonNull ParsedRecordDto source) {
    ParsedRecord parsedRecord = source.getParsedRecord();

    try (InputStream input = IOUtils
      .toInputStream(new ObjectMapper().writeValueAsString(parsedRecord), StandardCharsets.UTF_8)) {
      Record marcRecord = new MarcJsonReader(input).next();
      String leader = masqueradeBlanks(marcRecord.getLeader().marshal());

      List<FieldItem> fields = marcRecord.getControlFields()
        .stream()
        .map(cf -> controlFieldToQuickMarcField(cf, leader))
        .collect(Collectors.toList());

      fields.addAll(marcRecord.getDataFields()
        .stream()
        .map(this::dataFieldToQuickMarcField)
        .collect(Collectors.toList()));

      return new QuickMarc().parsedRecordId(UUID.fromString(parsedRecord.getId()))
        .leader(leader)
        .fields(fields)
        .parsedRecordDtoId(UUID.fromString(source.getId()))
        .externalId(getExternalId(source))
        .externalHrid(getExternalHrId(source))
        .marcFormat(supportedType())
        .suppressDiscovery(source.getAdditionalInfo().getSuppressDiscovery())
        .updateInfo(new UpdateInfo()
          .recordState(UpdateInfo.RecordStateEnum.fromValue(source.getRecordState().value()))
          .updateDate(convertDate(source)));
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  protected abstract UUID getExternalId(ParsedRecordDto source);
  protected abstract String getExternalHrId(ParsedRecordDto source);
  protected abstract Map<String, Object> splitGeneralInformationControlField(String content, String leader);

  private OffsetDateTime convertDate(ParsedRecordDto parsedRecordDto) {
    var updatedDate = parsedRecordDto.getMetadata().getUpdatedDate();
    return updatedDate != null ? OffsetDateTime.ofInstant(updatedDate.toInstant(), ZoneId.systemDefault()) : null;
  }

  private Object processControlField(ControlField controlField, String leader) {
    switch (controlField.getTag()) {
      case ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD:
        return splitAdditionalCharacteristicsControlField(masqueradeBlanks(controlField.getData()));
      case PHYSICAL_DESCRIPTIONS_CONTROL_FIELD:
        return splitPhysicalDescriptionsControlField(masqueradeBlanks(controlField.getData()));
      case GENERAL_INFORMATION_CONTROL_FIELD:
        return splitGeneralInformationControlField(masqueradeBlanks(controlField.getData()), leader);
      default:
        return controlField.getData();
    }
  }

  private Map<String, Object> splitAdditionalCharacteristicsControlField(String content) {
    return fillContentMap(AdditionalMaterialConfiguration.resolveByCode(content.charAt(0)).getControlFieldItems(), content,
      0);
  }

  private Map<String, Object> splitPhysicalDescriptionsControlField(String content) {
    var physicalDescription = PhysicalDescriptionFixedFieldElements
      .resolveByCode(content.charAt(0));
    Map<String, Object> contentMap = new LinkedHashMap<>();
    contentMap.put(Constants.CATEGORY_NAME, physicalDescription.getName());
    physicalDescription.getControlFieldItems()
      .forEach(item -> contentMap.put(item.getName(), getControlFieldItemVal(content, item)));
    return contentMap;
  }

  private String getControlFieldItemVal(String content, ControlFieldItem element) {
    return element.getLength() != 0 ? extractElementFromContent(content, element, 0) : content;
  }

  protected Map<String, Object> fillContentMap(List<ControlFieldItem> items, String content, int delta) {
    return items.stream()
      .collect(
        toMap(ControlFieldItem::getName, element -> getControlFieldElementContent(content, element, delta), (o, o2) -> o,
          LinkedHashMap::new));
  }

  private Object getControlFieldElementContent(String content, ControlFieldItem element, int delta) {
    var elementFromContent = extractElementFromContent(content, element, delta);
    return element.isArray()
      ? Arrays.asList(elementFromContent.split(EMPTY))
      : elementFromContent;
  }

  private String extractElementFromContent(String content, ControlFieldItem element, int delta) {
    return StringUtils
      .substring(content, element.getPosition() + delta, element.getPosition() + delta + element.getLength());
  }

  private FieldItem dataFieldToQuickMarcField(DataField field) {
    return new FieldItem()
      .isProtected(isProtected(field))
      .tag(field.getTag())
      .indicators(Arrays.asList(masqueradeBlanks(Character.toString(field.getIndicator1())),
        masqueradeBlanks(Character.toString(field.getIndicator2()))))
      .content(field.getSubfields()
        .stream()
        .map(subfield -> new StringBuilder("$").append(subfield.getCode())
          .append(SPACE)
          .append(subfield.getData()))
        .collect(Collectors.joining(SPACE)));
  }

  private FieldItem controlFieldToQuickMarcField(ControlField field, String leader) {
    return new FieldItem()
      .isProtected(isProtected(field))
      .tag(field.getTag())
      .content(processControlField(field, leader))
      .indicators(Collections.emptyList());
  }

  private String masqueradeBlanks(String sourceString) {
    return sourceString.replace(SPACE, BLANK_REPLACEMENT);
  }

  private boolean isProtected(ControlField field) {
    return fieldProtectionSettingsMarc.getMarcFieldProtectionSettings().stream()
      .filter(setting -> isAnyValueInSettingOrTagMatch(setting, field))
      .anyMatch(setting -> isAnyDataInSettingOrDataMatch(setting, field));
  }

  private boolean isProtected(DataField field) {
    return fieldProtectionSettingsMarc.getMarcFieldProtectionSettings().stream()
      .filter(setting -> isAnyFieldInSettingOrFieldMatch(setting, field))
      .filter(setting -> isAnyIndicator1InSettingOrIndicator1Match(setting, field))
      .filter(setting -> isAnyIndicator2InSettingOrIndicator2Match(setting, field))
      .filter(setting -> isAnySubFieldInSettingOrSubFieldMatch(setting, field))
      .anyMatch(setting -> isAnyDataInSettingOrDataMatch(setting, field));
  }

  private boolean isAnyValueInSettingOrTagMatch(MarcFieldProtectionSetting setting, ControlField field) {
    return (isBlank(setting.getIndicator1()) && isBlank(setting.getIndicator2()) && isBlank(setting.getSubfield()))
      && setting.getField().equals(ANY_STRING) || setting.getField().equals(field.getTag());
  }

  private boolean isAnyDataInSettingOrDataMatch(MarcFieldProtectionSetting setting, ControlField field) {
    return setting.getData().equals(ANY_STRING) || setting.getData().equals(field.getData());
  }

  private boolean isAnyFieldInSettingOrFieldMatch(MarcFieldProtectionSetting setting, DataField field) {
    return setting.getField().equals(ANY_STRING) || setting.getField().equals(field.getTag());
  }

  private boolean isAnyIndicator1InSettingOrIndicator1Match(MarcFieldProtectionSetting setting, DataField field) {
    return setting.getIndicator1().equals(ANY_STRING)
      || (isNotEmpty(setting.getIndicator1()) ? setting.getIndicator1().charAt(0) : BLANK_SUBFIELD_CODE)
      == field.getIndicator1();
  }

  private boolean isAnyIndicator2InSettingOrIndicator2Match(MarcFieldProtectionSetting setting, DataField field) {
    return setting.getIndicator2().equals(ANY_STRING)
      || (isNotEmpty(setting.getIndicator2()) ? setting.getIndicator2().charAt(0) : BLANK_SUBFIELD_CODE)
      == field.getIndicator2();
  }

  private boolean isAnySubFieldInSettingOrSubFieldMatch(MarcFieldProtectionSetting setting, DataField field) {
    return setting.getSubfield().equals(ANY_STRING) || field.getSubfield(setting.getSubfield().charAt(0)) != null;
  }

  private boolean isAnyDataInSettingOrDataMatch(MarcFieldProtectionSetting setting, DataField field) {
    return setting.getData().equals(ANY_STRING) || setting.getData()
      .equals(field.getSubfield(setting.getSubfield().charAt(0)).getData());
  }

}
