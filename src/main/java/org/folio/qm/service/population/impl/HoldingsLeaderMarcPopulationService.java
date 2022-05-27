package org.folio.qm.service.population.impl;

import org.springframework.stereotype.Service;

import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.LeaderMarcPopulationService;
import static org.folio.qm.converter.elements.Constants.COMMON_CONSTANT_LEADER_ITEMS;

import java.util.List;

@Service
public class HoldingsLeaderMarcPopulationService extends LeaderMarcPopulationService {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat.equals(MarcFormat.HOLDINGS);
  }

  @Override
  protected List<LeaderItem> getConstantLeaderItems() {
    return COMMON_CONSTANT_LEADER_ITEMS;
  }
}
