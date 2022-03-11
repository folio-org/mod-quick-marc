package org.folio.qm.converter;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;

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
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.folio.qm.converternew.VariableFieldConverter;
import org.folio.qm.converternew.dto.AdditionalCharacteristicsControlFieldConverter;
import org.folio.qm.converternew.dto.CommonControlFieldConverter;
import org.folio.qm.converternew.dto.CommonDataFieldConverter;
import org.folio.qm.converternew.dto.GeneralInformationAuthorityControlFieldConverter;
import org.folio.qm.converternew.dto.GeneralInformationBibliographicControlFieldConverter;
import org.folio.qm.converternew.dto.GeneralInformationHoldingsControlFieldConverter;
import org.folio.qm.converternew.dto.PhysicalMaterialControlFieldConverter;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSetting;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.springframework.lang.NonNull;

import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.UpdateInfo;
import org.folio.qm.exception.ConverterException;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@RequiredArgsConstructor
public abstract class AbstractMarcDtoConverter implements MarcDtoConverter {
  private static final String ANY_STRING = "*";
  private static final char BLANK_SUBFIELD_CODE = ' ';

  private final MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc;
  private List<VariableFieldConverter<DataField>> dataFieldConverters = List.of(
    new CommonDataFieldConverter()
  );
  private List<VariableFieldConverter<ControlField>> controlFieldConverters = List.of(
    new AdditionalCharacteristicsControlFieldConverter(),
    new CommonControlFieldConverter(), new GeneralInformationAuthorityControlFieldConverter(),
    new GeneralInformationHoldingsControlFieldConverter(), new GeneralInformationBibliographicControlFieldConverter(),
    new PhysicalMaterialControlFieldConverter()
  );

  @Override
  public QuickMarc convert(@NonNull ParsedRecordDto source) {
    ParsedRecord parsedRecord = source.getParsedRecord();

    try (InputStream input = IOUtils
      .toInputStream(new ObjectMapper().writeValueAsString(parsedRecord), StandardCharsets.UTF_8)) {
      Record marcRecord = new MarcJsonReader(input).next();
      String leader = masqueradeBlanks(marcRecord.getLeader().marshal());

      MarcFormat marcFormat;
      switch (source.getRecordType()) {
        case MARC_BIB:
          marcFormat = MarcFormat.BIBLIOGRAPHIC;
          break;
        case MARC_AUTHORITY:
          marcFormat = MarcFormat.AUTHORITY;
          break;
        case MARC_HOLDING:
          marcFormat = MarcFormat.HOLDINGS;
          break;
        default:
          marcFormat = null;
      }

      List<FieldItem> fields = marcRecord.getControlFields()
        .stream()
        .map(cf -> controlFieldToQuickMarcField(cf, marcRecord.getLeader(), marcFormat))
        .collect(Collectors.toList());

      fields.addAll(marcRecord.getDataFields()
        .stream()
        .map(field -> dataFieldToQuickMarcField(field, marcRecord.getLeader(), marcFormat))
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

  private OffsetDateTime convertDate(ParsedRecordDto parsedRecordDto) {
    var updatedDate = parsedRecordDto.getMetadata().getUpdatedDate();
    return updatedDate != null ? OffsetDateTime.ofInstant(updatedDate.toInstant(), ZoneId.systemDefault()) : null;
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

  private FieldItem dataFieldToQuickMarcField(DataField field, Leader leader, MarcFormat marcFormat) {
    return dataFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"))
      .isProtected(isProtected(field));
  }

  private FieldItem controlFieldToQuickMarcField(ControlField field, Leader leader, MarcFormat marcFormat) {
    return controlFieldConverters.stream()
      .filter(converter -> converter.canProcess(field, marcFormat))
      .findFirst()
      .map(converter -> converter.convert(field, leader))
      .orElseThrow(() -> new IllegalArgumentException("No data field converter found"))
      .isProtected(isProtected(field))
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
