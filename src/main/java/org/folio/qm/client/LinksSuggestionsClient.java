package org.folio.qm.client;

import org.folio.qm.client.model.EntitiesLinksSuggestions;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "links-suggestions", accept = MediaType.APPLICATION_JSON_VALUE)
public interface LinksSuggestionsClient {

  @HttpExchange("/marc")
  EntitiesLinksSuggestions postLinksSuggestions(@RequestBody EntitiesLinksSuggestions srsMarcRecord,
                                                @RequestParam AuthoritySearchParameter authoritySearchParameter,
                                                @RequestParam Boolean ignoreAutoLinkingEnabled);
}
