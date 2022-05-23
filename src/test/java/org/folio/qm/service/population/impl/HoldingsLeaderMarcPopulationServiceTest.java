package org.folio.qm.service.population.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;

@UnitTest
class HoldingsLeaderMarcPopulationServiceTest {

  private final HoldingsLeaderMarcPopulationService populationService = new HoldingsLeaderMarcPopulationService();

  @Test
  void shouldSupportHoldingsFormat() {
    assertTrue(populationService.supportFormat(MarcFormat.HOLDINGS));
  }
}
