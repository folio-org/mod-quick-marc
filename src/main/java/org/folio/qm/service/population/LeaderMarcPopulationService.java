package org.folio.qm.service.population;

import static org.folio.qm.converter.elements.Constants.LEADER_LENGTH;

import java.util.List;

import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.QuickMarc;

public abstract class LeaderMarcPopulationService implements MarcPopulationService {

  @Override
  public void populate(QuickMarc qmRecord) {
    var initialLeader = qmRecord.getLeader();
    if (LEADER_LENGTH != initialLeader.length()) {
      return;
    }

    var leader = populateValues(initialLeader, getConstantLeaderItems());

    qmRecord.setLeader(leader);
  }

  protected abstract List<LeaderItem> getConstantLeaderItems();

  protected String populateValues(String leader, List<LeaderItem> leaderItems) {
    var leaderBuilder = new StringBuilder(leader);

    leaderItems.stream()
      .filter(leaderItem -> leaderItem.getPossibleValues().size() == 1)
      .forEach(leaderItem -> leaderBuilder.setCharAt(leaderItem.getPosition(), leaderItem.getPossibleValues().get(0)));

    return leaderBuilder.toString();
  }
}
