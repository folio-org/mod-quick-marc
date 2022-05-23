package org.folio.qm.service.population.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.support.types.UnitTest;

@UnitTest
class LeaderMarcPopulationServiceTest {

  private final HoldingsLeaderMarcPopulationService populationService = new HoldingsLeaderMarcPopulationService();

  private static final String VALID_LEADER = "00241cx\\\\a2200109zn\\4500";
  private static final String WRONG_INDICATOR_COUNT = "00241cx\\\\a0200109zn\\4500";
  private static final String WRONG_SUBFIELD_CODE_LENGTH = "00241cx\\\\a2000109zn\\4500";
  private static final String WRONG_ENTRY_MAP_20 = "00241cx\\\\a2200109zn\\5500";
  private static final String WRONG_ENTRY_MAP_21 = "00241cx\\\\a2200109zn\\4400";
  private static final String WRONG_ENTRY_MAP_22 = "00241cx\\\\a2200109zn\\4510";
  private static final String WRONG_ENTRY_MAP_23 = "00241cx\\\\a2200109zn\\4501";

  @Test
  void shouldReturnInitialLeaderIfValid() {
    var leader = VALID_LEADER;
    var quickMarc = getQuickMarc(leader);

    populationService.populate(quickMarc);

    assertEquals(leader, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnIndicatorCount() {
    var quickMarc = getQuickMarc(WRONG_INDICATOR_COUNT);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnSubfieldCodeLength() {
    var quickMarc = getQuickMarc(WRONG_SUBFIELD_CODE_LENGTH);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap20() {
    var quickMarc = getQuickMarc(WRONG_ENTRY_MAP_20);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap21() {
    var quickMarc = getQuickMarc(WRONG_ENTRY_MAP_21);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap22() {
    var quickMarc = getQuickMarc(WRONG_ENTRY_MAP_22);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap23() {
    var quickMarc = getQuickMarc(WRONG_ENTRY_MAP_23);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  private QuickMarc getQuickMarc(String leader) {
    return new QuickMarc()
      .leader(leader);
  }
}
