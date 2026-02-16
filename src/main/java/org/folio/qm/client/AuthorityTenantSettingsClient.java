package org.folio.qm.client;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "authorities/config")
public interface AuthorityTenantSettingsClient {

  @GetMapping(value = "/groups/authorities/settings")
  Optional<SettingCollection> getAuthoritiesSettings();

  @Getter
  enum AuthoritySettingsKey {
    MAPPING_EXTENDED("mapping.extended");

    private final String key;

    AuthoritySettingsKey(String key) {
      this.key = key;
    }
  }

  record SettingCollection(List<Setting> settings) { }

  record Setting(String key, Object value) { }
}
