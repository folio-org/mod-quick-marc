package org.folio.qm.service.storage.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.client.AuthorityTenantSettingsClient.AuthoritySettingsKey.MAPPING_EXTENDED;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.folio.qm.client.AuthorityTenantSettingsClient;
import org.folio.qm.client.AuthorityTenantSettingsClient.Setting;
import org.folio.qm.client.AuthorityTenantSettingsClient.SettingCollection;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthoritiesConfigServiceImplTest {

  private static final String MAPPING_EXTENDED_KEY = MAPPING_EXTENDED.getKey();
  private static final String OTHER_KEY = "other.key";

  @Mock
  private AuthorityTenantSettingsClient settingsClient;

  @InjectMocks
  private AuthoritiesConfigServiceImpl service;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(settingsClient);
  }

  @Test
  void isAuthorityExtendedMappingEnabled_positive_returnsTrue() {
    var setting = new Setting(MAPPING_EXTENDED_KEY, true);
    var settingCollection = new SettingCollection(List.of(setting));

    when(settingsClient.getAuthoritiesSettings()).thenReturn(Optional.of(settingCollection));

    boolean result = service.isAuthorityExtendedMappingEnabled();

    assertThat(result).isTrue();
  }

  @Test
  void isAuthorityExtendedMappingEnabled_positive_returnsFalse() {
    var setting = new Setting(MAPPING_EXTENDED_KEY, false);
    var settingCollection = new SettingCollection(List.of(setting));

    when(settingsClient.getAuthoritiesSettings()).thenReturn(Optional.of(settingCollection));

    boolean result = service.isAuthorityExtendedMappingEnabled();

    assertThat(result).isFalse();
  }

  @Test
  void isAuthorityExtendedMappingEnabled_negative_emptySettings() {
    when(settingsClient.getAuthoritiesSettings()).thenReturn(Optional.empty());

    boolean result = service.isAuthorityExtendedMappingEnabled();

    assertThat(result).isFalse();
  }

  @Test
  void isAuthorityExtendedMappingEnabled_negative_emptySettingsCollection() {
    var settingCollection = new SettingCollection(Collections.emptyList());

    when(settingsClient.getAuthoritiesSettings()).thenReturn(Optional.of(settingCollection));

    boolean result = service.isAuthorityExtendedMappingEnabled();

    assertThat(result).isFalse();
  }

  @Test
  void isAuthorityExtendedMappingEnabled_negative_settingNotFound() {
    var otherSetting = new Setting(OTHER_KEY, true);
    var settingCollection = new SettingCollection(List.of(otherSetting));

    when(settingsClient.getAuthoritiesSettings()).thenReturn(Optional.of(settingCollection));

    boolean result = service.isAuthorityExtendedMappingEnabled();

    assertThat(result).isFalse();
  }

  @Test
  void isAuthorityExtendedMappingEnabled_positive_multipleSettingsWithMatchingKey() {
    var otherSetting = new Setting(OTHER_KEY, false);
    var targetSetting = new Setting(MAPPING_EXTENDED_KEY, true);
    var settingCollection = new SettingCollection(List.of(otherSetting, targetSetting));

    when(settingsClient.getAuthoritiesSettings()).thenReturn(Optional.of(settingCollection));

    boolean result = service.isAuthorityExtendedMappingEnabled();

    assertThat(result).isTrue();
  }
}
