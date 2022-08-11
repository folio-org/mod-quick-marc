package org.folio.qm.service.population.impl;

import static org.folio.qm.converter.elements.Constants.HOLDINGS_CONSTANT_LEADER_ITEMS;

import java.util.List;
import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.LeaderMarcPopulationService;
import org.springframework.stereotype.Service;

@Service
public class HoldingsLeaderMarcPopulationService extends LeaderMarcPopulationService {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat.equals(MarcFormat.HOLDINGS);
  }

  @Override
  protected List<LeaderItem> getConstantLeaderItems() {
    return HOLDINGS_CONSTANT_LEADER_ITEMS;
  }
}
