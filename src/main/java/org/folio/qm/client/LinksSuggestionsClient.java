package org.folio.qm.client;

import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.model.LinksSuggestions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "links-suggestions", accept = MediaType.APPLICATION_JSON_VALUE)
public interface LinksSuggestionsClient {

  @PostExchange("/marc")
  LinksSuggestions postLinksSuggestions(@RequestBody LinksSuggestions linksSuggestions,
                                        @RequestParam AuthoritySearchParameter authoritySearchParameter,
                                        @RequestParam Boolean ignoreAutoLinkingEnabled);
}
