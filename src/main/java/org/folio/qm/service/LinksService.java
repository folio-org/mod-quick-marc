package org.folio.qm.service;

import java.util.List;
import org.folio.qm.client.LinksClient;
import org.folio.qm.domain.dto.QuickMarc;

public interface LinksService {

  void setRecordLinks(QuickMarc qmRecord);

  void updateRecordLinks(QuickMarc qmRecord);

  List<LinksClient.LinkingRuleDto> getLinkingRules();
}
