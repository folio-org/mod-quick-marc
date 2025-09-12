package org.folio.qm.service.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.folio.qm.converter.elements.Constants.CONTROL_FIELD_PATTERN;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.FieldProtectionSettingsClient;
import org.folio.qm.client.model.MarcFieldProtectionSetting;
import org.folio.qm.client.model.MarcFieldProtectionSettingsCollection;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.service.FieldProtectionSetterService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldProtectionSetterServiceImpl implements FieldProtectionSetterService {

  private static final String ANY_STRING = "*";
  private static final String BLANK_SUBFIELD_CODE = "\\";

  private final FieldProtectionSettingsClient dicsClient;

  @Override
  public void applyFieldProtection(QuickMarcView qmRecord) {
    var fieldProtectionSettings = dicsClient.getFieldProtectionSettings();
    qmRecord.getFields().forEach(field -> setProtectedStatus(field, fieldProtectionSettings));
  }

  private void setProtectedStatus(FieldItem field, MarcFieldProtectionSettingsCollection settings) {
    if (CONTROL_FIELD_PATTERN.matcher(field.getTag()).matches()) {
      if (field.getContent() instanceof Map) {
        field.setIsProtected(isProtectedSpecialControlField(field, settings));
      } else {
        field.setIsProtected(isProtectedControlField(field, settings));
      }
    } else {
      field.setIsProtected(isProtected(field, settings));
    }
  }

  private boolean isProtectedSpecialControlField(FieldItem field, MarcFieldProtectionSettingsCollection settings) {
    return settings.getMarcFieldProtectionSettings().stream()
      .anyMatch(setting -> isAnyValueInSettingOrTagMatch(setting, field));
  }

  private boolean isProtectedControlField(FieldItem field, MarcFieldProtectionSettingsCollection settings) {
    return settings.getMarcFieldProtectionSettings().stream()
      .filter(setting -> isAnyValueInSettingOrTagMatch(setting, field))
      .anyMatch(setting -> isAnyDataInSettingOrDataMatch(setting, field));
  }

  private boolean isProtected(FieldItem field, MarcFieldProtectionSettingsCollection fieldProtectionSettings) {
    return fieldProtectionSettings.getMarcFieldProtectionSettings().stream()
      .filter(setting -> isAnyFieldInSettingOrFieldMatch(setting, field))
      .filter(setting -> isAnyIndicator1InSettingOrIndicator1Match(setting, field))
      .filter(setting -> isAnyIndicator2InSettingOrIndicator2Match(setting, field))
      .filter(setting -> isAnySubFieldInSettingOrSubFieldMatch(setting, field))
      .anyMatch(setting -> isAnyDataInSettingOrDataMatchWithSubfield(setting, field));
  }

  private boolean isAnyValueInSettingOrTagMatch(MarcFieldProtectionSetting setting, FieldItem field) {
    return isBlank(setting.getIndicator1()) && isBlank(setting.getIndicator2()) && isBlank(setting.getSubfield())
           && setting.getField().equals(ANY_STRING) || setting.getField().equals(field.getTag());
  }

  private boolean isAnyDataInSettingOrDataMatch(MarcFieldProtectionSetting setting, FieldItem field) {
    return setting.getData().equals(ANY_STRING) || setting.getData().equals(field.getContent().toString());
  }

  private boolean isAnyFieldInSettingOrFieldMatch(MarcFieldProtectionSetting setting, FieldItem field) {
    return setting.getField().equals(ANY_STRING) || setting.getField().equals(field.getTag());
  }

  private boolean isAnyIndicator1InSettingOrIndicator1Match(MarcFieldProtectionSetting setting, FieldItem field) {
    return Objects.equals(setting.getIndicator1(), ANY_STRING)
           || (isNotEmpty(setting.getIndicator1()) ? setting.getIndicator1() : BLANK_SUBFIELD_CODE)
             .equals(field.getIndicators().getFirst());
  }

  private boolean isAnyIndicator2InSettingOrIndicator2Match(MarcFieldProtectionSetting setting, FieldItem field) {
    return Objects.equals(setting.getIndicator2(), ANY_STRING)
           || (isNotEmpty(setting.getIndicator2()) ? setting.getIndicator2() : BLANK_SUBFIELD_CODE)
             .equals(field.getIndicators().get(1));
  }

  private boolean isAnySubFieldInSettingOrSubFieldMatch(MarcFieldProtectionSetting setting, FieldItem field) {
    return Objects.equals(setting.getSubfield(), ANY_STRING)
           || Pattern.compile("[$]" + setting.getSubfield()).matcher(field.getContent().toString()).find();
  }

  private boolean isAnyDataInSettingOrDataMatchWithSubfield(MarcFieldProtectionSetting setting, FieldItem field) {
    if (Objects.equals(setting.getData(), ANY_STRING)) {
      return true;
    }

    var subfieldRegex = Objects.equals(setting.getSubfield(), ANY_STRING) ? "." : setting.getSubfield();
    var dataPattern = Pattern.compile(".*\\$" + subfieldRegex + " " + Pattern.quote(setting.getData()) + ".*");
    return dataPattern.matcher(field.getContent().toString()).matches();
  }
}
