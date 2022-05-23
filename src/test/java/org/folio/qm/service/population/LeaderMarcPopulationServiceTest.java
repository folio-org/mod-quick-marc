package org.folio.qm.service.population;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.folio.qm.service.population.impl.HoldingsLeaderMarcPopulationService;
import org.folio.qm.support.types.UnitTest;

@UnitTest
class LeaderMarcPopulationServiceTest {

  private final HoldingsLeaderMarcPopulationService populationService = new HoldingsLeaderMarcPopulationService();

  private static final String VALID_LEADER = "00241cx\\\\a2200109zn\\4500";
  private static final String INVALID_LEADER_LENGTH = "00241cx\\\\a2200109zn\\450";
  private static final String WRONG_INDICATOR_COUNT = "00241cx\\\\a0200109zn\\4500";
  private static final String WRONG_SUBFIELD_CODE_LENGTH = "00241cx\\\\a2000109zn\\4500";
  private static final String WRONG_ENTRY_MAP_20 = "00241cx\\\\a2200109zn\\5500";
  private static final String WRONG_ENTRY_MAP_21 = "00241cx\\\\a2200109zn\\4400";
  private static final String WRONG_ENTRY_MAP_22 = "00241cx\\\\a2200109zn\\4510";
  private static final String WRONG_ENTRY_MAP_23 = "00241cx\\\\a2200109zn\\4501";

  @Test
  void shouldReturnInitialLeaderIfValid() {
    var expectedLeader = VALID_LEADER;
    var actualLeader = populationService.populate(expectedLeader);

    assertEquals(expectedLeader, actualLeader);
  }

  @Test
  void shouldReturnInitialLeaderIfWrongLength() {
    var expectedLeader = INVALID_LEADER_LENGTH;
    var actualLeader = populationService.populate(expectedLeader);

    assertEquals(expectedLeader, actualLeader);
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnIndicatorCount() {
    var leader = populationService.populate(WRONG_INDICATOR_COUNT);
    assertEquals(VALID_LEADER, leader);
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnSubfieldCodeLength() {
    var leader = populationService.populate(WRONG_SUBFIELD_CODE_LENGTH);
    assertEquals(VALID_LEADER, leader);
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap20() {
    var leader = populationService.populate(WRONG_ENTRY_MAP_20);
    assertEquals(VALID_LEADER, leader);
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap21() {
    var leader = populationService.populate(WRONG_ENTRY_MAP_21);
    assertEquals(VALID_LEADER, leader);
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap22() {
    var leader = populationService.populate(WRONG_ENTRY_MAP_22);
    assertEquals(VALID_LEADER, leader);
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnEntryMap23() {
    var leader = populationService.populate(WRONG_ENTRY_MAP_23);
    assertEquals(VALID_LEADER, leader);
  }
}
