package org.folio.qm.service.population.impl;

import org.springframework.stereotype.Service;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.LeaderMarcPopulationService;

@Service
public class BibliographicLeaderMarcPopulationService extends LeaderMarcPopulationService {

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat.equals(MarcFormat.BIBLIOGRAPHIC);
  }
}
