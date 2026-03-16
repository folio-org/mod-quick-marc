package org.folio.qm.service.storage.config;

import static org.folio.qm.client.AuthorityTenantSettingsClient.AuthoritySettingsKey.MAPPING_EXTENDED;
import static org.folio.qm.config.CacheConfig.AUTHORITIES_EXTENDED_MAPPING_CACHE;

import java.util.Collections;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.AuthorityTenantSettingsClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AuthoritiesConfigServiceImpl implements AuthoritiesConfigService {

  private static final String AUTHORITY_EXTENDED_MAPPING_NOT_FOUND_MESSAGE =
    "Authority extended mapping setting not found, falling back to false";
  private final AuthorityTenantSettingsClient settingsClient;

  public AuthoritiesConfigServiceImpl(AuthorityTenantSettingsClient settingsClient) {
    this.settingsClient = settingsClient;
  }

  @Override
  @Cacheable(cacheNames = AUTHORITIES_EXTENDED_MAPPING_CACHE, key = "@folioExecutionContext.tenantId")
  public boolean isAuthorityExtendedMappingEnabled() {
    var extendedMappingSetting = settingsClient.getAuthoritiesSettings()
      .map(AuthorityTenantSettingsClient.SettingCollection::settings)
      .orElse(Collections.emptyList()).stream()
      .filter(setting -> MAPPING_EXTENDED.getKey().equals(setting.key()))
      .findFirst()
      .map(AuthorityTenantSettingsClient.Setting::value)
      .map(Boolean.class::cast);
    if (extendedMappingSetting.isEmpty()) {
      var warnMsg = "isAuthorityExtendedMappingEnabled:: %s".formatted(AUTHORITY_EXTENDED_MAPPING_NOT_FOUND_MESSAGE);
      log.warn(warnMsg);
    }
    return extendedMappingSetting.orElse(false);
  }
}
