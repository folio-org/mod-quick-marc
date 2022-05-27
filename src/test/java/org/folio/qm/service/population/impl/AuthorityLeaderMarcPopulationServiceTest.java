package org.folio.qm.service.population.impl;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class AuthorityLeaderMarcPopulationServiceTest {

  private final AuthorityLeaderMarcPopulationService populationService = new AuthorityLeaderMarcPopulationService();

  private static final String VALID_LEADER = "06059cz\\\\a2201201n\\\\4500";
  private static final String WRONG_AUTHORITY_RECORD_TYPE = "06059ca\\\\a2201201n\\\\4500";

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

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityRecordType() {
    var quickMarc = getQuickMarc(WRONG_AUTHORITY_RECORD_TYPE);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  private QuickMarc getQuickMarc(String leader) {
    return new QuickMarc()
      .leader(leader);
  }
}
