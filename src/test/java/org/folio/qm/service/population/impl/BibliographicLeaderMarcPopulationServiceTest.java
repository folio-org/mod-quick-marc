package org.folio.qm.service.population.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class BibliographicLeaderMarcPopulationServiceTest {

  private final BibliographicLeaderMarcPopulationService populationService =
    new BibliographicLeaderMarcPopulationService();

  @Test
  void shouldSupportBibliographicFormat() {
    assertTrue(populationService.supportFormat(MarcFormat.BIBLIOGRAPHIC));
  }

  @Test
  void shouldNotSupportHoldingsFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.HOLDINGS));
  }

  @Test
  void shouldNotSupportAuthorityFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.AUTHORITY));
  }
}
