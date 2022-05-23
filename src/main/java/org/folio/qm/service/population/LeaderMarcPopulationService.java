package org.folio.qm.service.population;

import static org.folio.qm.converter.elements.Constants.LEADER_LENGTH;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_20;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_21;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_22;
import static org.folio.qm.converter.elements.LeaderItem.ENTRY_MAP_23;
import static org.folio.qm.converter.elements.LeaderItem.INDICATOR_COUNT;
import static org.folio.qm.converter.elements.LeaderItem.SUBFIELD_CODE_LENGTH;

import java.util.LinkedList;
import java.util.List;
import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.QuickMarc;

public abstract class LeaderMarcPopulationService implements MarcPopulationService {

  private static final List<LeaderItem> COMMON_CONSTANT_LEADER_ITEMS =
    List.of(INDICATOR_COUNT, SUBFIELD_CODE_LENGTH,
      ENTRY_MAP_20, ENTRY_MAP_21, ENTRY_MAP_22, ENTRY_MAP_23);

  @Override
  public void populate(QuickMarc qmRecord) {
    var initialLeader = qmRecord.getLeader();
    if (LEADER_LENGTH != initialLeader.length()) {
      return;
    }

    var leader = populateValues(initialLeader, new LinkedList<>(COMMON_CONSTANT_LEADER_ITEMS));

    qmRecord.setLeader(leader);
  }

  protected String populateValues(String leader, List<LeaderItem> leaderItems) {
    var leaderBuilder = new StringBuilder(leader);

    leaderItems.stream()
      .filter(leaderItem -> leaderItem.getPossibleValues().size() == 1)
      .forEach(leaderItem -> leaderBuilder.setCharAt(leaderItem.getPosition(), leaderItem.getPossibleValues().get(0)));

    return leaderBuilder.toString();
  }
}
