package org.folio.qm.service.population.impl;

import java.util.Collections;

import org.springframework.stereotype.Service;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.LeaderMarcPopulationService;

@Service
public class HoldingsLeaderMarcPopulationService extends LeaderMarcPopulationService {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat.equals(MarcFormat.HOLDINGS);
  }

  @Override
  public String populateValues(String leader) {
    return populateValues(leader, Collections.emptyList());
  }
}
