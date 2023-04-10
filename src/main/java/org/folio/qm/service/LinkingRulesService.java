package org.folio.qm.service;

import java.util.List;
import org.folio.qm.client.LinkingRulesClient;

public interface LinkingRulesService {

  List<LinkingRulesClient.LinkingRuleDto> getLinkingRules();
}
