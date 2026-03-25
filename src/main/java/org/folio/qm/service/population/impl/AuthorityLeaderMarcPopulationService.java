package org.folio.qm.service.population.impl;

import static org.folio.qm.convertion.elements.Constants.AUTHORITY_CONSTANT_LEADER_ITEMS;

import java.util.List;
import org.folio.qm.convertion.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.LeaderMarcPopulationService;
import org.springframework.stereotype.Service;

@Service
public class AuthorityLeaderMarcPopulationService extends LeaderMarcPopulationService {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat.equals(MarcFormat.AUTHORITY);
  }

  @Override
  protected List<LeaderItem> getConstantLeaderItems() {
    return AUTHORITY_CONSTANT_LEADER_ITEMS;
  }
}
