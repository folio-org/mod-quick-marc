package org.folio.qm.service.population.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.StubQuickMarcRecord;
import org.junit.jupiter.api.Test;

@UnitTest
class HoldingsLeaderMarcPopulationServiceTest {

  private static final String VALID_LEADER = "00497cy\\\\a22001574\\\\4500";
  private static final String WRONG_UNDEFINED_CHARACTER_POSITION_7 = "00497cy7\\a22001574\\\\4500";
  private static final String WRONG_UNDEFINED_CHARACTER_POSITION_8 = "00497cy\\8a22001574\\\\4500";
  private static final String WRONG_UNDEFINED_CHARACTER_POSITION_19 = "00497cy\\\\a22001574\\94500";

  private final HoldingsLeaderMarcPopulationService populationService = new HoldingsLeaderMarcPopulationService();

  @Test
  void shouldSupportHoldingsFormat() {
    assertTrue(populationService.supportFormat(MarcFormat.HOLDINGS));
  }

  @Test
  void shouldNotSupportBibliographicFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.BIBLIOGRAPHIC));
  }

  @Test
  void shouldNotSupportAuthorityFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.AUTHORITY));
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityUndefinedCharacterPosition7() {
    var quickMarc = getQuickMarc(WRONG_UNDEFINED_CHARACTER_POSITION_7);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityUndefinedCharacterPosition8() {
    var quickMarc = getQuickMarc(WRONG_UNDEFINED_CHARACTER_POSITION_8);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityUndefinedCharacterPosition19() {
    var quickMarc = getQuickMarc(WRONG_UNDEFINED_CHARACTER_POSITION_19);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  private StubQuickMarcRecord getQuickMarc(String leader) {
    var marcRecord = new StubQuickMarcRecord();
    marcRecord.setLeader(leader);
    return marcRecord;
  }
}
