package org.folio.qm.service;

import java.util.List;
import org.folio.qm.client.LinksClient;

public interface LinkingRulesService {

  List<LinksClient.LinkingRuleDto> getLinkingRules();
}
