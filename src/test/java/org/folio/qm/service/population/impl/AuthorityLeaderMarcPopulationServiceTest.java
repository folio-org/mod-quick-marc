package org.folio.qm.service.population.impl;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class AuthorityLeaderMarcPopulationServiceTest {

  private final AuthorityLeaderMarcPopulationService populationService = new AuthorityLeaderMarcPopulationService();

  @Test
  void shouldSupportAuthorityFormat() {
    assertTrue(populationService.supportFormat(MarcFormat.AUTHORITY));
  }

  @Test
  void shouldNotSupportBibliographicFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.BIBLIOGRAPHIC));
  }

  @Test
  void shouldNotSupportHoldingsFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.HOLDINGS));
  }
}
