package org.folio.qm.service.population;

import static org.folio.qm.converter.elements.Constants.COMMON_CONSTANT_LEADER_ITEMS;
import static org.folio.qm.converter.elements.Constants.LEADER_LENGTH;

import java.util.LinkedList;
import java.util.List;

import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.QuickMarc;

public abstract class LeaderMarcPopulationService implements MarcPopulationService {

  @Override
  public void populate(QuickMarc qmRecord) {
    var leader = populate(qmRecord.getLeader());

    qmRecord.setLeader(leader);
  }

  protected abstract String populate(String leader);

  protected String populateValues(String leader, List<LeaderItem> customLeaderItems) {
    if (LEADER_LENGTH != leader.length()) {
      return leader;
    }

    var leaderBuilder = new StringBuilder(leader);
    var leaderItems = new LinkedList<>(COMMON_CONSTANT_LEADER_ITEMS);
    leaderItems.addAll(customLeaderItems);

    leaderItems.stream()
      .filter(leaderItem -> leaderItem.getPossibleValues().size() == 1)
      .forEach(leaderItem -> leaderBuilder.setCharAt(leaderItem.getPosition(), leaderItem.getPossibleValues().get(0)));

    return leaderBuilder.toString();
  }
}
