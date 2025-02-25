package org.folio.qm.service.population;

import static org.folio.qm.converter.elements.Constants.LEADER_LENGTH;

import java.util.List;
import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.BaseMarcRecord;

public abstract class LeaderMarcPopulationService implements MarcPopulationService {

  @Override
  public void populate(BaseMarcRecord qmRecord) {
    var initialLeader = qmRecord.getLeader();
    if (LEADER_LENGTH != initialLeader.length()) {
      return;
    }

    var leader = populateValues(initialLeader, getConstantLeaderItems());

    qmRecord.setLeader(leader);
  }

  protected abstract List<LeaderItem> getConstantLeaderItems();

  /**
   * Populates values.
   *
   * @param leader to populate default values
   *
   * @param leaderItems leader items that should be populated if leader has not acceptable value
   * @return cleaned leader
   */
  protected String populateValues(String leader, List<LeaderItem> leaderItems) {
    var leaderBuilder = new StringBuilder(leader);

    leaderItems.stream()
      .filter(leaderItem -> !leaderItem.getPossibleValues().contains(leaderBuilder.charAt(leaderItem.getPosition())))
      .forEach(leaderItem -> setLeaderChar(leaderItem, leaderBuilder));

    return leaderBuilder.toString();
  }

  private void setLeaderChar(LeaderItem leaderItem, StringBuilder leaderBuilder) {
    leaderBuilder.setCharAt(leaderItem.getPosition(), leaderItem.getPossibleValues().getFirst());
  }
}
