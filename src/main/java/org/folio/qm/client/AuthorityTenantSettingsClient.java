package org.folio.qm.client;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "authorities/config", accept = MediaType.APPLICATION_JSON_VALUE)
public interface AuthorityTenantSettingsClient {

  @GetExchange(value = "/groups/authorities/settings")
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
