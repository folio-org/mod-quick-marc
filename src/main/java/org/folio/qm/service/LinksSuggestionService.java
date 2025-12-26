package org.folio.qm.service;

import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.QuickMarcView;

public interface LinksSuggestionService {

  QuickMarcView suggestLinks(QuickMarcView quickMarcView,
                             AuthoritySearchParameter authoritySearchParameter,
                             Boolean ignoreAutoLinkingEnabled);
}
