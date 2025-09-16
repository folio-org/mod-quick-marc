package org.folio.qm.client;

import org.folio.qm.client.model.EntitiesLinksSuggestions;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "links-suggestions", dismiss404 = true)
public interface LinksSuggestionsClient {

  @PostMapping("/marc")
  EntitiesLinksSuggestions postLinksSuggestions(EntitiesLinksSuggestions srsMarcRecord,
                                                @RequestParam AuthoritySearchParameter authoritySearchParameter,
                                                @RequestParam Boolean ignoreAutoLinkingEnabled);
}
