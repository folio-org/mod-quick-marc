package org.folio.qm.client;

import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "field-protection-settings")
public interface DICSFieldProtectionSettingsClient {

  @GetMapping(value = "/marc?limit=1000", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  MarcFieldProtectionSettingsCollection getFieldProtectionSettingsMarc();

}
