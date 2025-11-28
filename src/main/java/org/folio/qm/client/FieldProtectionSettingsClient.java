package org.folio.qm.client;

import org.folio.qm.client.model.MarcFieldProtectionSettingsCollection;
import org.springframework.http.MediaType;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "field-protection-settings", accept = MediaType.APPLICATION_JSON_VALUE)
public interface FieldProtectionSettingsClient {

  @GetExchange(value = "/marc?limit=1000")
  MarcFieldProtectionSettingsCollection getFieldProtectionSettings();
}
