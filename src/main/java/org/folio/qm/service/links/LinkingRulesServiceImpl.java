package org.folio.qm.service.links;

import static org.folio.qm.config.CacheConfig.QM_FETCH_LINKING_RULES_RESULTS;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.LinkingRulesClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LinkingRulesServiceImpl implements LinkingRulesService {

  private final LinkingRulesClient linkingRulesClient;

  @Override
  @Cacheable(cacheNames = QM_FETCH_LINKING_RULES_RESULTS,
    key = "@folioExecutionContext.tenantId",
    unless = "#result.isEmpty()")
  public List<LinkingRulesClient.LinkingRuleDto> getLinkingRules() {
    log.debug("getLinkingRules:: Fetching linking rules");
    var rules = linkingRulesClient.fetchLinkingRules();
    log.info("getLinkingRules:: Retrieved {} linking rules", rules.size());
    return rules;
  }
}
