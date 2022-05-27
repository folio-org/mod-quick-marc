package org.folio.qm.service.population.impl;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.LeaderMarcPopulationService;
import org.springframework.stereotype.Service;

@Service
public class AuthorityLeaderMarcPopulationService extends LeaderMarcPopulationService {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat.equals(MarcFormat.AUTHORITY);
  }
}
