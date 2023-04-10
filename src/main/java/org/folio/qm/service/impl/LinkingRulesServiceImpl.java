package org.folio.qm.service.impl;

import static org.folio.qm.config.CacheNames.QM_FETCH_LINKING_RULES_RESULTS;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.LinkingRulesClient;
import org.folio.qm.service.LinkingRulesService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkingRulesServiceImpl implements LinkingRulesService {

  private final LinkingRulesClient linkingRulesClient;

  @Override
  @Cacheable(cacheNames = QM_FETCH_LINKING_RULES_RESULTS,
    key = "@folioExecutionContext.tenantId",
    unless = "#result.isEmpty()")
  public List<LinkingRulesClient.LinkingRuleDto> getLinkingRules() {
    return linkingRulesClient.fetchLinkingRules();
  }
}
