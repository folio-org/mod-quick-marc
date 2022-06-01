package org.folio.qm.service.population;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.folio.qm.converter.elements.Constants.COMMON_CONSTANT_LEADER_ITEMS;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.support.types.UnitTest;

@UnitTest
class LeaderMarcPopulationServiceTest {

  private final LeaderMarcPopulationService populationService = new LeaderMarcPopulationService() {
    @Override
    protected List<LeaderItem> getConstantLeaderItems() {
      return COMMON_CONSTANT_LEADER_ITEMS;
    }

    @Override
    public boolean supportFormat(MarcFormat marcFormat) {
      return true;
    }
  };

  private static final String VALID_LEADER = "00241cx\\\\a2200109zn\\4500";
  private static final String INVALID_LEADER_LENGTH = "00241cx\\\\a2200109zn\\450";
  private static final String WRONG_INDICATOR_COUNT = "00241cx\\\\a0200109zn\\4500";
  private static final String WRONG_SUBFIELD_CODE_LENGTH = "00241cx\\\\a2000109zn\\4500";
  private static final String WRONG_ENTRY_MAP_20 = "00241cx\\\\a2200109zn\\5500";
  private static final String WRONG_ENTRY_MAP_21 = "00241cx\\\\a2200109zn\\4400";
  private static final String WRONG_ENTRY_MAP_22 = "00241cx\\\\a2200109zn\\4510";
  private static final String WRONG_ENTRY_MAP_23 = "00241cx\\\\a2200109zn\\4501";
  private static final String WRONG_CODING_SCHEME = "00241cx\\\\g2200109zn\\4500";

  @Test
  void shouldReturnInitialLeaderIfValid() {
    var leader = VALID_LEADER;
    var quickMarc = getQuickMarc(leader);

    populationService.populate(quickMarc);

    assertEquals(leader, quickMarc.getLeader());
  }

  @Test
  void shouldReturnInitialLeaderIfWrongLength() {
    var leader = INVALID_LEADER_LENGTH;
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

  @Test
  void shouldNotUpdatePositionWithMultiplePossibleValues() {
    var customPopulationService = getPopulationServiceForCodingScheme();

    var leader = WRONG_CODING_SCHEME;
    var quickMarc = getQuickMarc(leader);
    customPopulationService.populate(quickMarc);

    assertEquals(leader, quickMarc.getLeader());
  }

  private LeaderMarcPopulationService getPopulationServiceForCodingScheme() {
    return new LeaderMarcPopulationService() {
      @Override
      public boolean supportFormat(MarcFormat marcFormat) {
        return true;
      }

      @Override
      protected List<LeaderItem> getConstantLeaderItems() {
        return new LinkedList<>(COMMON_CONSTANT_LEADER_ITEMS);
      }

      @Override
      protected String populateValues(String leader, List<LeaderItem> leaderItems) {
        leaderItems.add(LeaderItem.CODING_SCHEME);
        return super.populateValues(leader, leaderItems);
      }
    };
  }

  private QuickMarc getQuickMarc(String leader) {
    return new QuickMarc()
      .leader(leader);
  }
}
